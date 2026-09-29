package ru.matule.network.impl.error

import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response
import ru.matule.network.result.NetworkError
import ru.matule.network.result.NetworkResult
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Тело ошибки PocketBase: `{"status": 400, "message": "...", "data": {"field": {"code": "...", "message": "..."}}}`.
 * Старые версии присылают `code` вместо `status`.
 *
 * @property status HTTP-код.
 * @property code HTTP-код (старые версии PocketBase).
 * @property message текст сервера.
 * @property data ошибки по полям.
 */
@Serializable
internal data class ErrorResponseDto(
    val status: Int? = null,
    val code: Int? = null,
    val message: String = "",
    val data: Map<String, FieldErrorDto> = emptyMap(),
)

/**
 * Ошибка конкретного поля.
 *
 * @property code код проверки, например `validation_required`.
 * @property message текст сервера.
 */
@Serializable
internal data class FieldErrorDto(
    val code: String = "",
    val message: String = "",
)

/**
 * Превращает HTTP-ответ с ошибкой в [NetworkError] с понятным текстом на русском.
 *
 * @param json настройки разбора JSON.
 */
internal class ErrorParser(private val json: Json) {

    /**
     * Разбирает ошибку сервера.
     *
     * @param httpCode HTTP-код ответа.
     * @param body тело ответа или `null`.
     * @return ошибка для пользователя.
     */
    fun parse(httpCode: Int, body: String?): NetworkError {
        val dto = body
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { json.decodeFromString(ErrorResponseDto.serializer(), it) }.getOrNull() }
        val serverMessage = dto?.message.orEmpty()
        val fieldErrors = dto?.data.orEmpty().mapValues { (field, error) -> translate(field, error) }
        return when (httpCode) {
            400 -> NetworkError.Validation(
                message = fieldErrors.values.firstOrNull() ?: "Проверьте введённые данные",
                serverMessage = serverMessage,
                fieldErrors = fieldErrors,
            )
            401 -> NetworkError.Unauthorized()
            403 -> NetworkError.Forbidden()
            404 -> NetworkError.NotFound()
            in 500..599 -> NetworkError.Server(httpCode, "Ошибка сервера ($httpCode). Попробуйте позже")
            else -> NetworkError.Server(httpCode, "Ошибка сервера ($httpCode)")
        }
    }

    /**
     * Переводит ошибку поля PocketBase на русский.
     *
     * @param field имя поля.
     * @param error ошибка поля.
     * @return текст для пользователя.
     */
    fun translate(field: String, error: FieldErrorDto): String {
        val fieldName = FIELD_NAMES[field] ?: field
        return when (error.code) {
            "validation_required" -> "Заполните поле «$fieldName»"
            "validation_is_email", "validation_invalid_email" -> "Некорректный email"
            "validation_not_unique", "validation_invalid_email_or_already_used" ->
                if (field == "email") "Пользователь с таким email уже зарегистрирован" else "Значение «$fieldName» уже используется"
            "validation_length_out_of_range", "validation_min_text_constraint", "validation_length_too_short" ->
                "Недопустимая длина поля «$fieldName»"
            "validation_values_mismatch" -> "Пароли не совпадают"
            else -> error.message.ifBlank { "Ошибка в поле «$fieldName»" }
        }
    }

    private companion object {
        val FIELD_NAMES = mapOf(
            "email" to "Почта",
            "password" to "Пароль",
            "passwordConfirm" to "Повтор пароля",
            "name" to "Имя",
            "patronymic" to "Отчество",
            "surname" to "Фамилия",
            "birthday" to "Дата рождения",
            "gender" to "Пол",
            "title" to "Название",
            "image" to "Фото",
            "count" to "Количество",
        )
    }
}

/**
 * Выполняет запрос и превращает любые исключения в [NetworkResult.Failure]:
 * HTTP-ошибки → разбор тела, таймаут → [NetworkError.Timeout], нет сети → [NetworkError.NoConnection],
 * неверный JSON → [NetworkError.Parsing]. Отмена корутины пробрасывается дальше.
 *
 * @param errorParser разбор тела ошибки.
 */
internal class SafeApiCaller(private val errorParser: ErrorParser) {

    /**
     * Выполняет запрос безопасно.
     *
     * @param block сам запрос.
     * @return результат запроса.
     */
    suspend fun <T> call(block: suspend () -> T): NetworkResult<T> = try {
        NetworkResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        NetworkResult.Failure(errorParser.parse(e.code(), body))
    } catch (e: SocketTimeoutException) {
        NetworkResult.Failure(NetworkError.Timeout())
    } catch (e: IOException) {
        NetworkResult.Failure(NetworkError.NoConnection())
    } catch (e: SerializationException) {
        NetworkResult.Failure(NetworkError.Parsing())
    } catch (e: Exception) {
        NetworkResult.Failure(NetworkError.Unknown(e.message ?: "Неизвестная ошибка"))
    }
}

/**
 * Для запросов без тела ответа: превращает неуспешный HTTP-код в [HttpException],
 * чтобы его разобрал [SafeApiCaller].
 */
internal fun Response<Unit>.requireSuccess() {
    if (!isSuccessful) throw HttpException(this)
}
