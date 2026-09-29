package ru.matule.app.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import ru.matule.app.domain.model.Project
import ru.matule.app.domain.model.ProjectDraft
import ru.matule.app.domain.repository.AppStateRepository
import ru.matule.app.domain.repository.ProjectImageRepository
import ru.matule.app.domain.repository.ProjectRepository
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.validation.DateValidator

/** Проекты как поток. */
class ObserveProjectsUseCase(private val projects: ProjectRepository) {
    operator fun invoke(): StateFlow<List<Project>> = projects.projects
}

/** Загрузка списка проектов. */
class RefreshProjectsUseCase(private val projects: ProjectRepository) {
    suspend operator fun invoke(): AppResult<List<Project>> = projects.refresh()
}

/** Один проект. */
class GetProjectUseCase(private val projects: ProjectRepository) {
    suspend operator fun invoke(projectId: String): AppResult<Project> = projects.project(projectId)
}

/** Черновик проекта: чтение. */
class GetProjectDraftUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke(): ProjectDraft? = appState.projectDraft()
}

/** Черновик проекта: автосохранение. */
class SaveProjectDraftUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke(draft: ProjectDraft) = appState.saveProjectDraft(draft)
}

/** Подготовка фото проекта из галереи или камеры. */
class ImportProjectImageUseCase(private val images: ProjectImageRepository) {
    suspend operator fun invoke(sourceUri: String): AppResult<String> = images.importImage(sourceUri)
}

/**
 * Создание проекта: все поля обязательны, даты существуют и окончание не раньше начала.
 */
class CreateProjectUseCase(
    private val projects: ProjectRepository,
    private val images: ProjectImageRepository,
    private val appState: AppStateRepository,
) {
    suspend operator fun invoke(draft: ProjectDraft): AppResult<Project> {
        val fields = listOf(draft.type, draft.title, draft.recipient, draft.source, draft.category)
        if (fields.any { it.isBlank() } || draft.startDigits.isBlank() || draft.endDigits.isBlank()) {
            return AppResult.Failure(AppError.Validation("Заполните все поля"))
        }
        val start = DateValidator.toLocalDate(draft.startDigits)
            ?: return AppResult.Failure(AppError.Validation("Введите существующую дату начала", FIELD_START))
        val end = DateValidator.toLocalDate(draft.endDigits)
            ?: return AppResult.Failure(AppError.Validation("Введите существующую дату окончания", FIELD_END))
        if (end.isBefore(start)) {
            return AppResult.Failure(AppError.Validation("Окончание не может быть раньше начала", FIELD_END))
        }
        val imagePath = draft.imagePath
            ?: return AppResult.Failure(AppError.Validation("Добавьте фото проекта", FIELD_IMAGE))
        val image = images.load(imagePath)
            ?: return AppResult.Failure(AppError.Validation("Фото не найдено, добавьте его снова", FIELD_IMAGE))
        val result = projects.create(draft.copy(title = draft.title.trim(), source = draft.source.trim()), image)
        if (result is AppResult.Success) appState.clearProjectDraft()
        return result
    }

    companion object {
        /** Поле «Дата начала». */
        const val FIELD_START = "start_date"

        /** Поле «Дата окончания». */
        const val FIELD_END = "end_date"

        /** Фото. */
        const val FIELD_IMAGE = "image"
    }
}
