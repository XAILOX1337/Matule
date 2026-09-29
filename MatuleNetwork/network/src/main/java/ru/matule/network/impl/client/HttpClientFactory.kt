package ru.matule.network.impl.client

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.matule.network.config.NetworkConfig
import ru.matule.network.config.TokenProvider
import java.util.concurrent.TimeUnit

/** Общие настройки JSON: неизвестные поля пропускаются, значения по умолчанию отправляются. */
@OptIn(ExperimentalSerializationApi::class)
internal val NetworkJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
    coerceInputValues = true
    isLenient = true
}

/**
 * Добавляет заголовок `Authorization` с токеном пользователя к каждому запросу, если токен есть.
 *
 * @param tokenProvider источник токена.
 */
internal class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.getToken()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder().header("Authorization", token).build()
        }
        return chain.proceed(request)
    }
}

/**
 * Собирает OkHttp и Retrofit по настройкам.
 *
 * @param config настройки клиента.
 * @param tokenProvider источник токена.
 * @param json настройки сериализации.
 */
internal class HttpClientFactory(
    private val config: NetworkConfig,
    private val tokenProvider: TokenProvider,
    private val json: Json = NetworkJson,
) {
    /**
     * Создаёт Retrofit с интерсептором авторизации и, при необходимости, логированием.
     *
     * @return настроенный Retrofit.
     */
    fun createRetrofit(): Retrofit {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenProvider))
        if (config.enableLogging) {
            clientBuilder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
            )
        }
        return Retrofit.Builder()
            .baseUrl(config.normalizedBaseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
