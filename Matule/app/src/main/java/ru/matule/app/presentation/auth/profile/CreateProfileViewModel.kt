package ru.matule.app.presentation.auth.profile

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
import ru.matule.app.domain.model.ProfileDraft
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.GetProfileDraftUseCase
import ru.matule.app.domain.usecase.SaveProfileDraftUseCase
import ru.matule.app.domain.usecase.SubmitProfileDraftUseCase
import ru.matule.app.domain.usecase.ValidateEmailUseCase
import ru.matule.app.domain.validation.DateValidator
import ru.matule.app.domain.validation.InputField
import ru.matule.app.presentation.common.Formatters
import ru.matule.app.presentation.common.Notifier
import java.time.LocalDate
import java.util.Locale

/**
 * Состояние экрана «Создание профиля».
 *
 * @property name имя.
 * @property patronymic отчество.
 * @property surname фамилия.
 * @property birthdayDigits дата рождения цифрами.
 * @property gender пол.
 * @property email почта.
 * @property emailError ошибка под полем почты.
 * @property birthdayError ошибка под полем даты.
 * @property isLoading идёт проверка на сервере.
 */
data class CreateProfileUiState(
    val name: String = "",
    val patronymic: String = "",
    val surname: String = "",
    val birthdayDigits: String = "",
    val gender: String = "",
    val email: String = "",
    val emailError: String? = null,
    val birthdayError: String? = null,
    val isLoading: Boolean = false,
) {
    /** Все поля обязательны — кнопка «Далее» активна, когда заполнены все. */
    val isFilled: Boolean
        get() = listOf(name, patronymic, surname, birthdayDigits, gender, email).all { it.isNotBlank() }

    /** Дата рождения, как в макете: «28 февраля 1991». */
    val birthdayText: String
        get() = birthdayDate?.let(Formatters::longDate).orEmpty()

    /** Дата рождения или `null`. */
    val birthdayDate: LocalDate?
        get() = DateValidator.toLocalDate(birthdayDigits)

    /** Черновик для сохранения. */
    fun toDraft(): ProfileDraft = ProfileDraft(name, patronymic, surname, birthdayDigits, gender, email)
}

/**
 * Экран «Создание профиля»: автосохранение черновика (переживает перезапуск), проверка полей
 * и проверка почты на сервере.
 *
 * @param getDraft сохранённый черновик.
 * @param saveDraft автосохранение черновика.
 * @param submitDraft проверка и переход к созданию пароля.
 * @param validateEmail проверка формата почты.
 * @param notifier уведомления об ошибках.
 */
class CreateProfileViewModel(
    private val getDraft: GetProfileDraftUseCase,
    private val saveDraft: SaveProfileDraftUseCase,
    private val submitDraft: SubmitProfileDraftUseCase,
    private val validateEmail: ValidateEmailUseCase,
    private val notifier: Notifier,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateProfileUiState())

    /** Состояние экрана. */
    val state: StateFlow<CreateProfileUiState> = _state.asStateFlow()

    private val nextEvents = Channel<Unit>(Channel.BUFFERED)

    /** Профиль заполнен верно — переход к созданию пароля. */
    val next: Flow<Unit> = nextEvents.receiveAsFlow()

    private var edited = false
    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            val draft = getDraft() ?: return@launch
            if (!edited) {
                _state.value = CreateProfileUiState(
                    name = draft.name,
                    patronymic = draft.patronymic,
                    surname = draft.surname,
                    birthdayDigits = draft.birthdayDigits,
                    gender = draft.gender,
                    email = draft.email,
                )
            }
        }
    }

    /** Ввод имени. */
    fun onNameChange(value: String) = edit { copy(name = value) }

    /** Ввод отчества. */
    fun onPatronymicChange(value: String) = edit { copy(patronymic = value) }

    /** Ввод фамилии. */
    fun onSurnameChange(value: String) = edit { copy(surname = value) }

    /** Выбор даты рождения. */
    fun onBirthdayPicked(date: LocalDate) = edit {
        copy(
            birthdayDigits = String.format(Locale.ROOT, "%02d%02d%04d", date.dayOfMonth, date.monthValue, date.year),
            birthdayError = null,
        )
    }

    /** Выбор пола. */
    fun onGenderChange(value: String) = edit { copy(gender = value) }

    /** Ввод почты. */
    fun onEmailChange(value: String) = edit { copy(email = value, emailError = null) }

    /** «Далее». */
    fun submit() {
        val current = _state.value
        if (!current.isFilled || current.isLoading) return
        if (!validateEmail(current.email)) {
            _state.update { it.copy(emailError = ValidateEmailUseCase.EMAIL_ERROR) }
            return
        }
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = submitDraft(current.toDraft())
            _state.update { it.copy(isLoading = false) }
            when (result) {
                is AppResult.Success -> nextEvents.send(Unit)
                is AppResult.Failure -> showError(result.error)
            }
        }
    }

    private fun edit(transform: CreateProfileUiState.() -> CreateProfileUiState) {
        edited = true
        _state.update(transform)
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(SAVE_DELAY_MS)
            saveDraft(_state.value.toDraft())
        }
    }

    private fun showError(error: AppError) {
        when {
            error is AppError.Validation && error.field == InputField.EMAIL ->
                _state.update { it.copy(emailError = error.message) }
            error is AppError.Validation && error.field == InputField.BIRTHDAY ->
                _state.update { it.copy(birthdayError = error.message) }
            else -> notifier.showError(error)
        }
    }

    private companion object {
        const val SAVE_DELAY_MS = 300L
    }
}
