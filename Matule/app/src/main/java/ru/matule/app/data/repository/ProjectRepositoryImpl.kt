package ru.matule.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.matule.app.data.local.SessionLocalDataSource
import ru.matule.app.data.remote.ProjectRemoteDataSource
import ru.matule.app.domain.model.ImageFile
import ru.matule.app.domain.model.Project
import ru.matule.app.domain.model.ProjectDraft
import ru.matule.app.domain.repository.ProjectRepository
import ru.matule.app.domain.repository.UserDataCleaner
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.result.map
import ru.matule.app.domain.result.onSuccess

/**
 * Проекты: сервер + список в памяти для экранов.
 *
 * @param remote запросы проектов.
 * @param session сессия (идентификатор пользователя).
 */
class ProjectRepositoryImpl(
    private val remote: ProjectRemoteDataSource,
    private val session: SessionLocalDataSource,
) : ProjectRepository, UserDataCleaner {

    private val state = MutableStateFlow<List<Project>>(emptyList())

    override val projects: StateFlow<List<Project>> = state.asStateFlow()

    override suspend fun refresh(): AppResult<List<Project>> {
        val userId = session.load()?.userId ?: return unauthorized()
        return remote.projects(userId).map { list ->
            val sorted = list.sortedByDescending { it.createdAtMillis }
            state.value = sorted
            sorted
        }
    }

    override suspend fun project(projectId: String): AppResult<Project> {
        state.value.firstOrNull { it.id == projectId }?.let { return AppResult.Success(it) }
        return remote.project(projectId)
    }

    override suspend fun create(draft: ProjectDraft, image: ImageFile): AppResult<Project> {
        val userId = session.load()?.userId ?: return unauthorized()
        return remote.create(userId, draft, image).onSuccess { created ->
            state.update { listOf(created) + it.filterNot { project -> project.id == created.id } }
        }
    }

    override suspend fun clearUserData() {
        state.value = emptyList()
    }

    private fun unauthorized() = AppResult.Failure(AppError.Unauthorized("Войдите в аккаунт"))
}
