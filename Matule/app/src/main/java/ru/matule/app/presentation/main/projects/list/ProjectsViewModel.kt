package ru.matule.app.presentation.main.projects.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.matule.app.domain.model.Project
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.ObserveProjectsUseCase
import ru.matule.app.domain.usecase.RefreshProjectsUseCase
import ru.matule.app.presentation.common.Notifier

/**
 * Состояние экрана «Проекты».
 *
 * @property projects проекты, новые первыми.
 * @property isLoading первая загрузка.
 */
data class ProjectsUiState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
)

/**
 * Экран «Проекты»: список с сервера. Новый проект появляется в списке сразу после создания.
 *
 * @param observeProjects проекты как поток.
 * @param refreshProjects загрузка списка.
 * @param notifier уведомления об ошибках.
 */
class ProjectsViewModel(
    observeProjects: ObserveProjectsUseCase,
    private val refreshProjects: RefreshProjectsUseCase,
    private val notifier: Notifier,
) : ViewModel() {

    private val loading = MutableStateFlow(true)

    /** Состояние экрана. */
    val state: StateFlow<ProjectsUiState> = combine(observeProjects(), loading) { projects, isLoading ->
        ProjectsUiState(projects = projects, isLoading = isLoading && projects.isEmpty())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ProjectsUiState(isLoading = true))

    init {
        refresh()
    }

    /** Загрузить список заново. */
    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            val result = refreshProjects()
            loading.value = false
            if (result is AppResult.Failure) notifier.showError(result.error)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
