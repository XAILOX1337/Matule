package ru.matule.app.data.remote.network

import ru.matule.app.data.remote.ProfileRemoteDataSource
import ru.matule.app.domain.model.User
import ru.matule.app.domain.result.AppResult
import ru.matule.network.MatuleApi
import ru.matule.network.model.UpdateProfileRequestDto

/**
 * Профиль через библиотеку MatuleNetwork.
 *
 * @param api клиент сервера.
 */
class NetworkProfileRemoteDataSource(private val api: MatuleApi) : ProfileRemoteDataSource {

    override suspend fun getProfile(userId: String): AppResult<User> =
        api.profile.getProfile(userId).toAppResult { it.toDomain() }

    override suspend fun setNotifications(userId: String, enabled: Boolean): AppResult<User> =
        api.profile.updateProfile(userId, UpdateProfileRequestDto(notifications = enabled)).toAppResult { it.toDomain() }
}
