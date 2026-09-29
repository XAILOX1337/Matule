package ru.matule.network.config

/**
 * Настройки сетевого клиента.
 *
 * @property baseUrl адрес сервера PocketBase, например `http://10.0.2.2:8090/`.
 * @property enableLogging писать ли запросы и ответы в Logcat (включать только в отладочной сборке).
 * @property connectTimeoutSeconds таймаут подключения к серверу, в секундах.
 * @property readTimeoutSeconds таймаут чтения ответа и отправки тела запроса, в секундах.
 */
data class NetworkConfig(
    val baseUrl: String,
    val enableLogging: Boolean = false,
    val connectTimeoutSeconds: Long = 15,
    val readTimeoutSeconds: Long = 30,
) {
    /** Адрес сервера со слешем в конце — так требует Retrofit. */
    val normalizedBaseUrl: String
        get() = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
}

/**
 * Источник токена авторизации. Реализуется приложением: библиотека не хранит сессию сама
 * и берёт токен перед каждым запросом.
 */
fun interface TokenProvider {
    /**
     * Возвращает текущий токен пользователя.
     *
     * @return токен или `null`, если пользователь не авторизован.
     */
    fun getToken(): String?
}
