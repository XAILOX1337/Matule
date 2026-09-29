package ru.matule.app.data.remote

import ru.matule.app.domain.model.ProfileDraft
import ru.matule.app.domain.model.User
import ru.matule.app.domain.result.AppResult

/**
 * Сессия, выданная сервером.
 *
 * @property token токен авторизации.
 * @property user пользователь.
 */
data class RemoteSession(
    val token: String,
    val user: User,
)

/**
 * Запросы авторизации. Абстракция над сетевой библиотекой: репозитории не знают,
 * какой клиент ходит на сервер (реализация — `data/remote/network`).
 */
interface AuthRemoteDataSource {
    /** Авторизация по почте и паролю. */
    suspend fun signIn(email: String, password: String): AppResult<RemoteSession>

    /** Свободна ли почта для регистрации. */
    suspend fun isEmailAvailable(email: String): AppResult<Boolean>

    /** Создание пользователя. */
    suspend fun signUp(draft: ProfileDraft, password: String): AppResult<Unit>

    /** Обновление токена. */
    suspend fun refresh(): AppResult<RemoteSession>

    /** Выход на сервере. */
    suspend fun logout(): AppResult<Unit>
}
