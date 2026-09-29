package ru.matule.app.data.remote

import ru.matule.app.domain.model.User
import ru.matule.app.domain.result.AppResult

/**
 * Запросы профиля. Абстракция над сетевой библиотекой.
 */
interface ProfileRemoteDataSource {
    /** Получение информации о профиле. */
    suspend fun getProfile(userId: String): AppResult<User>

    /** Изменение профиля: включение и выключение уведомлений. */
    suspend fun setNotifications(userId: String, enabled: Boolean): AppResult<User>
}
