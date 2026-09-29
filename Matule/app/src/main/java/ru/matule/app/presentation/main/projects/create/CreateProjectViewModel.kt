package ru.matule.app.presentation.main.projects.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.matule.app.domain.model.ProjectDraft
import ru.matule.app.domain.repository.AnalyticsTracker
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.CreateProjectUseCase
import ru.matule.app.domain.usecase.GetProjectDraftUseCase
import ru.matule.app.domain.usecase.ImportProjectImageUseCase
import ru.matule.app.domain.usecase.SaveProjectDraftUseCase
import ru.matule.app.domain.validation.DateValidator
import ru.matule.app.presentation.common.Notifier

/**
 * Состояние экрана «Создать проект».
 *
 * @property type тип.
 * @property title название проекта.
 * @property startDigits дата начала цифрами.
 * @property endDigits дата окончания цифрами.
 * @property recipient кому.
 * @property source источник описания.
 * @property category категория.
 * @property imagePath путь к фото.
 * @property startError ошибка даты начала.
 * @property endError ошибка даты окончания.
 * @property imageError подсветить плитку фото.
 * @property isImportingImage фото обрабатывается.
 * @property isSaving идёт отправка.
 */
data class CreateProjectUiState(
    val type: String = "",
    val title: String = "",
    val startDigits: String = "",
    val endDigits: String = "",
    val recipient: String = "",
    val source: String = "",
    val category: String = "",
    val imagePath: String? = null,
    val startError: String? = null,
    val endError: String? = null,
    val imageError: Boolean = false,
    val isImportingImage: Boolean = false,
    val isSaving: Boolean = false,
) {
    /** Все поля обязательны — «Подтвердить» активна, когда заполнено всё, включая фото. */
    val isFilled: Boolean
        get() = listOf(type, title, recipient, source, category).all { it.isNotBlank() } &&
            startDigits.length == DATE_DIGITS && endDigits.length == DATE_DIGITS && imagePath != null

    /** Черновик. */
    fun toDraft(): ProjectDraft = ProjectDraft(type, title, startDigits, endDigits, recipient, source, category, imagePath)

    private companion object {
        const val DATE_DIGITS = 8
    }
}

/** Откуда фото. */
enum class PhotoSource(val analyticsName: String) {
    Gallery("галерея"),
    Camera("камера"),
}

/**
 * События AppMetrica экрана «Создать проект»: по одному на каждое поле и на отправку.
 */
object ProjectEvents {
    const val TYPE = "Создание проекта: тип"
    const val TITLE = "Создание проекта: название"
    const val START_DATE = "Создание проекта: дата начала"
    const val END_DATE = "Создание проекта: дата окончания"
    const val RECIPIENT = "Создание проекта: кому"
    const val SOURCE = "Создание проекта: источник описания"
    const val CATEGORY = "Создание проекта: категория"
    const val PHOTO = "Создание проекта: фото"
    const val SUBMIT = "Создание проекта: отправка"
}

/**
 * Экран «Создать проект»: все поля обязательны, черновик переживает перезапуск,
 * каждое поле и отправка отмечаются в аналитике.
 *
 * @param getDraft сохранённый черновик.
 * @param saveDraft автосохранение черновика.
 * @param importImage подготовка фото.
 * @param createProject создание проекта на сервере.
 * @param analytics аналитика.
 * @param notifier уведомления.
 */
class CreateProjectViewModel(
    private val getDraft: GetProjectDraftUseCase,
    private val saveDraft: SaveProjectDraftUseCase,
    private val importImage: ImportProjectImageUseCase,
    private val createProject: CreateProjectUseCase,
    private val analytics: AnalyticsTracker,
    private val notifier: Notifier,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateProjectUiState())

    /** Состояние экрана. */
    val state: StateFlow<CreateProjectUiState> = _state.asStateFlow()

    private val createdEvents = Channel<Unit>(Channel.BUFFERED)

    /** Проект создан. */
    val created: Flow<Unit> = createdEvents.receiveAsFlow()

    private var edited = false
    private var saveJob: Job? = null
    private val trackJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            val draft = getDraft() ?: return@launch
            if (!edited) {
                _state.value = CreateProjectUiState(
                    type = draft.type,
                    title = draft.title,
                    startDigits = draft.startDigits,
                    endDigits = draft.endDigits,
                    recipient = draft.recipient,
                    source = draft.source,
                    category = draft.category,
                    imagePath = draft.imagePath,
                )
            }
        }
    }

    /** Выбор типа. */
    fun onTypeChange(value: String) = edit(ProjectEvents.TYPE, value) { copy(type = value) }

    /** Ввод названия. */
    fun onTitleChange(value: String) = edit(ProjectEvents.TITLE) { copy(title = value) }

    /** Ввод даты начала. */
    fun onStartChange(digits: String) = edit(ProjectEvents.START_DATE) {
        copy(startDigits = digits, startError = dateError(digits))
    }

    /** Ввод даты окончания. */
    fun onEndChange(digits: String) = edit(ProjectEvents.END_DATE) {
        copy(endDigits = digits, endError = dateError(digits))
    }

    /** Выбор «Кому». */
    fun onRecipientChange(value: String) = edit(ProjectEvents.RECIPIENT, value) { copy(recipient = value) }

    /** Ввод источника описания. */
    fun onSourceChange(value: String) = edit(ProjectEvents.SOURCE) { copy(source = value) }

    /** Выбор категории. */
    fun onCategoryChange(value: String) = edit(ProjectEvents.CATEGORY, value) { copy(category = value) }

    /**
     * Фото выбрано в галерее или снято камерой.
     *
     * @param uri адрес фото.
     * @param source откуда фото.
     */
    fun onImagePicked(uri: String, source: PhotoSource) {
        _state.update { it.copy(isImportingImage = true) }
        viewModelScope.launch {
            val result = importImage(uri)
            _state.update { it.copy(isImportingImage = false) }
            when (result) {
                is AppResult.Success -> {
                    edit { copy(imagePath = result.data, imageError = false) }
                    analytics.track(ProjectEvents.PHOTO, mapOf("источник" to source.analyticsName))
                }
                is AppResult.Failure -> notifier.showError(result.error)
            }
        }
    }

    /** На телефоне нет приложения камеры. */
    fun onCameraUnavailable() {
        notifier.showError("Не удалось открыть камеру. Выберите фото из галереи")
    }

    /** «Подтвердить». */
    fun submit() {
        val current = _state.value
        if (!current.isFilled || current.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = createProject(current.toDraft())
            _state.update { it.copy(isSaving = false) }
            when (result) {
                is AppResult.Success -> {
                    analytics.track(ProjectEvents.SUBMIT, mapOf("результат" to "успех"))
                    notifier.showSuccess("Проект создан", "«${result.data.title}» появился в списке проектов")
                    createdEvents.send(Unit)
                }
                is AppResult.Failure -> {
                    analytics.track(ProjectEvents.SUBMIT, mapOf("результат" to "ошибка"))
                    showError(result.error)
                }
            }
        }
    }

    /**
     * Изменение поля: состояние, автосохранение черновика и событие аналитики.
     *
     * @param event событие; `null` — без аналитики.
     * @param selectedValue выбранный вариант селекта (отправляется сразу); `null` — текстовое поле,
     * событие уходит, когда пользователь перестал печатать.
     * @param transform изменение состояния.
     */
    private fun edit(
        event: String? = null,
        selectedValue: String? = null,
        transform: CreateProjectUiState.() -> CreateProjectUiState,
    ) {
        edited = true
        _state.update(transform)
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(SAVE_DELAY_MS)
            saveDraft(_state.value.toDraft())
        }
        if (event == null) return
        if (selectedValue != null) {
            analytics.track(event, mapOf("значение" to selectedValue))
        } else {
            trackJobs[event]?.cancel()
            trackJobs[event] = viewModelScope.launch {
                delay(TRACK_DELAY_MS)
                analytics.track(event)
            }
        }
    }

    private fun dateError(digits: String): String? =
        if (digits.length == 8 && !DateValidator.isValidDigits(digits)) "Такой даты не существует" else null

    private fun showError(error: AppError) {
        val field = (error as? AppError.Validation)?.field
        when (field) {
            CreateProjectUseCase.FIELD_START -> _state.update { it.copy(startError = error.message) }
            CreateProjectUseCase.FIELD_END -> _state.update { it.copy(endError = error.message) }
            CreateProjectUseCase.FIELD_IMAGE -> {
                _state.update { it.copy(imageError = true) }
                notifier.showError(error)
            }
            else -> notifier.showError(error)
        }
    }

    private companion object {
        const val SAVE_DELAY_MS = 300L
        const val TRACK_DELAY_MS = 1_000L
    }
}
