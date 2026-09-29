package ru.matule.app.data.local

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.matule.app.domain.repository.UserDataCleaner

/**
 * Сохранённая сессия.
 *
 * @property token токен авторизации.
 * @property userId идентификатор пользователя.
 */
data class StoredSession(
    val token: String,
    val userId: String,
)

/**
 * Сессия пользователя в DataStore. Токен дополнительно держится в памяти:
 * сетевой клиент запрашивает его перед каждым запросом.
 *
 * @param store хранилище настроек.
 */
class SessionLocalDataSource(private val store: PreferencesStore) : UserDataCleaner {

    private val mutex = Mutex()

    @Volatile
    private var cached: StoredSession? = null

    @Volatile
    private var loaded = false

    /** Сессия из памяти или из DataStore. */
    suspend fun load(): StoredSession? {
        if (loaded) return cached
        mutex.withLock {
            if (!loaded) {
                val token = store.getString(KEY_TOKEN)
                val userId = store.getString(KEY_USER_ID)
                cached = if (token != null && userId != null) StoredSession(token, userId) else null
                loaded = true
            }
        }
        return cached
    }

    /**
     * Токен для сетевого клиента. Вызывается из потока OkHttp, поэтому при первом обращении
     * допустимо синхронно дочитать DataStore.
     */
    fun currentToken(): String? {
        if (!loaded) runBlocking { load() }
        return cached?.token
    }

    /** Сохраняет сессию. */
    suspend fun save(session: StoredSession) {
        store.putStrings(mapOf(KEY_TOKEN to session.token, KEY_USER_ID to session.userId))
        cached = session
        loaded = true
    }

    override suspend fun clearUserData() {
        store.remove(KEY_TOKEN, KEY_USER_ID)
        cached = null
        loaded = true
    }

    private companion object {
        const val KEY_TOKEN = "session_token"
        const val KEY_USER_ID = "session_user_id"
    }
}
