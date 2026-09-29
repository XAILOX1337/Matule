package ru.matule.app.data.local

import ru.matule.app.domain.repository.UserDataCleaner
import java.security.MessageDigest
import java.util.UUID

/**
 * PIN-код для входа. Сам PIN не хранится: только соль и SHA-256 от «соль:PIN».
 *
 * @param store хранилище настроек.
 */
class PinLocalDataSource(private val store: PreferencesStore) : UserDataCleaner {

    /** Задан ли PIN. */
    suspend fun hasPin(): Boolean = store.getString(KEY_HASH) != null

    /** Сохраняет PIN с новой солью. */
    suspend fun savePin(pin: String) {
        val salt = UUID.randomUUID().toString()
        store.putStrings(mapOf(KEY_SALT to salt, KEY_HASH to hash(pin, salt)))
    }

    /** Совпадает ли PIN с сохранённым. */
    suspend fun verify(pin: String): Boolean {
        val salt = store.getString(KEY_SALT) ?: return false
        val expected = store.getString(KEY_HASH) ?: return false
        return MessageDigest.isEqual(hash(pin, salt).toByteArray(), expected.toByteArray())
    }

    override suspend fun clearUserData() = store.remove(KEY_SALT, KEY_HASH)

    private fun hash(pin: String, salt: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest("$salt:$pin".toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private companion object {
        const val KEY_SALT = "pin_salt"
        const val KEY_HASH = "pin_hash"
    }
}
