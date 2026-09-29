package ru.matule.network.result

/**
 * Результат сетевого запроса: успех с данными или ошибка.
 * Методы библиотеки никогда не бросают исключения наружу — любая проблема превращается в [Failure].
 *
 * @param T тип данных успешного ответа.
 */
sealed interface NetworkResult<out T> {

    /**
     * Успешный ответ сервера.
     *
     * @property data данные ответа.
     */
    data class Success<out T>(val data: T) : NetworkResult<T>

    /**
     * Запрос не выполнен.
     *
     * @property error причина ошибки с текстом для пользователя.
     */
    data class Failure(val error: NetworkError) : NetworkResult<Nothing>
}

/**
 * Преобразует данные успешного результата, ошибку оставляет без изменений.
 *
 * @param transform функция преобразования данных.
 * @return новый результат с преобразованными данными или та же ошибка.
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.Failure -> this
}

/**
 * Причина неудачного запроса. [message] — готовый текст на русском для показа пользователю.
 */
sealed interface NetworkError {
    /** Текст ошибки для пользователя. */
    val message: String

    /**
     * Нет соединения с сервером: нет интернета или сервер недоступен.
     *
     * @property message текст ошибки.
     */
    data class NoConnection(
        override val message: String = "Нет соединения с сервером. Проверьте подключение к интернету",
    ) : NetworkError

    /**
     * Сервер не ответил за отведённое время.
     *
     * @property message текст ошибки.
     */
    data class Timeout(
        override val message: String = "Сервер не отвечает. Попробуйте ещё раз",
    ) : NetworkError

    /**
     * Нужна авторизация или токен устарел (HTTP 401).
     *
     * @property message текст ошибки.
     */
    data class Unauthorized(
        override val message: String = "Сессия истекла. Войдите снова",
    ) : NetworkError

    /**
     * Нет прав на действие (HTTP 403).
     *
     * @property message текст ошибки.
     */
    data class Forbidden(
        override val message: String = "Недостаточно прав для этого действия",
    ) : NetworkError

    /**
     * Запись не найдена (HTTP 404).
     *
     * @property message текст ошибки.
     */
    data class NotFound(
        override val message: String = "Данные не найдены",
    ) : NetworkError

    /**
     * Сервер отклонил данные запроса (HTTP 400).
     *
     * @property message общий текст ошибки.
     * @property serverMessage исходный текст сервера (для отладки).
     * @property fieldErrors ошибки по полям: имя поля → текст ошибки на русском.
     */
    data class Validation(
        override val message: String,
        val serverMessage: String = "",
        val fieldErrors: Map<String, String> = emptyMap(),
    ) : NetworkError

    /**
     * Ошибка на стороне сервера или неожиданный HTTP-код.
     *
     * @property code HTTP-код ответа.
     * @property message текст ошибки.
     */
    data class Server(
        val code: Int,
        override val message: String,
    ) : NetworkError

    /**
     * Ответ сервера не удалось разобрать.
     *
     * @property message текст ошибки.
     */
    data class Parsing(
        override val message: String = "Не удалось обработать ответ сервера",
    ) : NetworkError

    /**
     * Любая другая ошибка.
     *
     * @property message текст ошибки.
     */
    data class Unknown(
        override val message: String = "Неизвестная ошибка",
    ) : NetworkError
}
