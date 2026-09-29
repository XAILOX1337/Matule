package ru.matule.app.data.repository

import ru.matule.app.data.local.ProjectImageStorage
import ru.matule.app.domain.model.ImageFile
import ru.matule.app.domain.repository.ProjectImageRepository
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import kotlin.coroutines.cancellation.CancellationException

/**
 * Фото проектов на устройстве.
 *
 * @param storage файлы фото.
 */
class ProjectImageRepositoryImpl(private val storage: ProjectImageStorage) : ProjectImageRepository {

    override suspend fun importImage(sourceUri: String): AppResult<String> = try {
        AppResult.Success(storage.import(sourceUri))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown("Не удалось открыть фото. Выберите другое"))
    }

    override suspend fun load(path: String): ImageFile? = storage.load(path)
}
