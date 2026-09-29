package ru.matule.app.data.repository

import ru.matule.app.data.local.ProfileLocalDataSource
import ru.matule.app.data.local.SessionLocalDataSource
import ru.matule.app.data.remote.ProfileRemoteDataSource
import ru.matule.app.domain.model.User
import ru.matule.app.domain.repository.ProfileRepository
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.result.map
import ru.matule.app.domain.result.onSuccess

/**
 * Профиль: сервер — источник правды, локальный кэш — для мгновенного показа и работы без сети.
 *
 * @param remote запросы профиля.
 * @param session сохранённая сессия (идентификатор пользователя).
 * @param local кэш профиля.
 */
class ProfileRepositoryImpl(
    private val remote: ProfileRemoteDataSource,
    private val session: SessionLocalDataSource,
    private val local: ProfileLocalDataSource,
) : ProfileRepository {

    override suspend fun getProfile(): AppResult<User> {
        val userId = session.load()?.userId ?: return AppResult.Failure(AppError.Unauthorized(NO_SESSION))
        return remote.getProfile(userId).onSuccess { local.saveProfile(it) }
    }

    override suspend fun cachedProfile(): User? = local.profile()

    override suspend fun isNotificationsEnabled(): Boolean = local.profile()?.notificationsEnabled ?: true

    override suspend fun setNotificationsEnabled(enabled: Boolean): AppResult<Unit> {
        val userId = session.load()?.userId ?: return AppResult.Failure(AppError.Unauthorized(NO_SESSION))
        return remote.setNotifications(userId, enabled).map { local.saveProfile(it) }
    }

    private companion object {
        const val NO_SESSION = "Войдите в аккаунт"
    }
}
