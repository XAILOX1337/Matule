package ru.matule.app.domain.result

/**
 * Результат операции бизнес-логики: успех с данными или ошибка.
 *
 * @param T тип данных при успехе.
 */
sealed interface AppResult<out T> {
    /** Успех. @property data данные. */
    data class Success<out T>(val data: T) : AppResult<T>

    /** Ошибка. @property error причина. */
    data class Failure(val error: AppError) : AppResult<Nothing>
}

/**
 * Причина ошибки. [message] — текст для пользователя.
 */
sealed interface AppError {
    /** Текст ошибки для пользователя. */
    val message: String

    /** Нет связи с сервером. */
    data class Connection(override val message: String) : AppError

    /** Сессия недействительна — нужен повторный вход. */
    data class Unauthorized(override val message: String) : AppError

    /**
     * Неверные данные.
     *
     * @property field поле, к которому относится ошибка (см. [ru.matule.app.domain.validation.InputField]);
     * `null` — ошибка формы целиком.
     */
    data class Validation(override val message: String, val field: String? = null) : AppError

    /** Ошибка сервера. */
    data class Server(override val message: String) : AppError

    /** Прочие ошибки. */
    data class Unknown(override val message: String) : AppError
}

/** Преобразует данные успешного результата. */
inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

/** Выполняет действие при успехе. */
inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

/** Выполняет действие при ошибке. */
inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}

/** Данные успеха или `null`. */
fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data
