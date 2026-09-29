package ru.matule.app.data.repository

import ru.matule.app.data.local.ProfileLocalDataSource
import ru.matule.app.data.local.SessionLocalDataSource
import ru.matule.app.data.local.StoredSession
import ru.matule.app.data.remote.AuthRemoteDataSource
import ru.matule.app.data.remote.RemoteSession
import ru.matule.app.domain.model.ProfileDraft
import ru.matule.app.domain.repository.AuthRepository
import ru.matule.app.domain.repository.UserDataCleaner
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.result.map

/**
 * Авторизация: сервер + локальная сессия.
 *
 * @param remote запросы авторизации.
 * @param session сохранённая сессия.
 * @param profileLocal кэш профиля (заполняется сразу после входа).
 * @param cleaners всё, что нужно очистить при выходе. Новое хранилище добавляется в список в DI,
 * а этот класс не меняется.
 */
class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val session: SessionLocalDataSource,
    private val profileLocal: ProfileLocalDataSource,
    private val cleaners: List<UserDataCleaner>,
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): AppResult<Unit> =
        remote.signIn(email, password).map { saveSession(it) }

    override suspend fun isEmailAvailable(email: String): AppResult<Boolean> = remote.isEmailAvailable(email)

    override suspend fun signUp(draft: ProfileDraft, password: String): AppResult<Unit> =
        when (val created = remote.signUp(draft, password)) {
            is AppResult.Failure -> created
            is AppResult.Success -> signIn(draft.email, password)
        }

    override suspend fun refreshSession(): AppResult<Unit> {
        if (session.load() == null) return AppResult.Failure(AppError.Unauthorized(NO_SESSION))
        return remote.refresh().map { saveSession(it) }
    }

    override suspend fun logout(): AppResult<Unit> {
        // Сначала сервер (нужен токен), потом данные на устройстве — они очищаются в любом случае.
        val result = if (session.load() != null) remote.logout() else AppResult.Success(Unit)
        cleaners.forEach { it.clearUserData() }
        return result
    }

    override suspend fun hasSession(): Boolean = session.load() != null

    override suspend fun currentUserId(): String? = session.load()?.userId

    private suspend fun saveSession(remoteSession: RemoteSession) {
        session.save(StoredSession(remoteSession.token, remoteSession.user.id))
        profileLocal.saveProfile(remoteSession.user)
    }

    private companion object {
        const val NO_SESSION = "Войдите в аккаунт"
    }
}
