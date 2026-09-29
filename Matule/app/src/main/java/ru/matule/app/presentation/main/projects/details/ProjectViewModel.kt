package ru.matule.app.presentation.main.projects.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.matule.app.domain.model.Project
import ru.matule.app.domain.repository.ReminderScheduler
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.GetProjectUseCase
import ru.matule.app.presentation.common.Notifier

/**
 * Состояние экрана проекта.
 *
 * @property project проект или `null`, пока не загружен.
 * @property isLoading идёт загрузка.
 */
data class ProjectUiState(
    val project: Project? = null,
    val isLoading: Boolean = true,
)

/**
 * Открытый проект. Любое действие пользователя в проекте переносит напоминание
 * «нет активности больше 3 минут».
 *
 * @param projectId идентификатор проекта.
 * @param getProject получение проекта.
 * @param reminders напоминания.
 * @param notifier уведомления об ошибках.
 */
class ProjectViewModel(
    projectId: String,
    getProject: GetProjectUseCase,
    private val reminders: ReminderScheduler,
    notifier: Notifier,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectUiState())

    /** Состояние экрана. */
    val state: StateFlow<ProjectUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getProject(projectId)) {
                is AppResult.Success -> {
                    _state.value = ProjectUiState(project = result.data, isLoading = false)
                    onUserActivity()
                }
                is AppResult.Failure -> {
                    _state.value = ProjectUiState(isLoading = false)
                    notifier.showError(result.error)
                }
            }
        }
    }

    /** Пользователь что-то сделал в проекте (касание, прокрутка). */
    fun onUserActivity() {
        val project = _state.value.project ?: return
        reminders.onProjectActivity(project.id, project.title)
    }
}
