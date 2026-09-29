package ru.matule.app.data.local

import kotlinx.serialization.Serializable
import ru.matule.app.domain.model.User
import ru.matule.app.domain.repository.UserDataCleaner

/**
 * Профиль в том виде, в котором он хранится на устройстве.
 */
@Serializable
private data class StoredUser(
    val id: String,
    val email: String,
    val name: String,
    val patronymic: String,
    val surname: String,
    val birthday: String,
    val gender: String,
    val notificationsEnabled: Boolean,
)

/**
 * Кэш профиля пользователя: экран «Профиль» показывает его сразу и без сети,
 * а фоновые уведомления берут из него настройку «Уведомления».
 *
 * @param store хранилище настроек.
 */
class ProfileLocalDataSource(private val store: PreferencesStore) : UserDataCleaner {

    /** Сохранённый профиль или `null`. */
    suspend fun profile(): User? = store.getJson(KEY_PROFILE, StoredUser.serializer())?.toDomain()

    /** Сохраняет профиль. */
    suspend fun saveProfile(user: User) = store.putJson(KEY_PROFILE, StoredUser.serializer(), user.toStored())

    override suspend fun clearUserData() = store.remove(KEY_PROFILE)

    private companion object {
        const val KEY_PROFILE = "profile_cache"
    }
}

private fun StoredUser.toDomain() = User(id, email, name, patronymic, surname, birthday, gender, notificationsEnabled)

private fun User.toStored() = StoredUser(id, email, name, patronymic, surname, birthday, gender, notificationsEnabled)
