package ru.matule.app.data.remote.network

import ru.matule.app.domain.model.User
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.network.model.UserDto
import ru.matule.network.result.NetworkError
import ru.matule.network.result.NetworkResult

/** Ошибка сетевой библиотеки → ошибка приложения (текст для пользователя сохраняется). */
fun NetworkError.toAppError(): AppError = when (this) {
    is NetworkError.NoConnection, is NetworkError.Timeout -> AppError.Connection(message)
    is NetworkError.Unauthorized -> AppError.Unauthorized(message)
    is NetworkError.Validation -> AppError.Validation(message, field = fieldErrors.keys.firstOrNull())
    is NetworkError.Forbidden, is NetworkError.NotFound, is NetworkError.Server, is NetworkError.Parsing ->
        AppError.Server(message)
    is NetworkError.Unknown -> AppError.Unknown(message)
}

/** Результат сетевой библиотеки → результат приложения с преобразованием данных. */
inline fun <T, R> NetworkResult<T>.toAppResult(transform: (T) -> R): AppResult<R> = when (this) {
    is NetworkResult.Success -> AppResult.Success(transform(data))
    is NetworkResult.Failure -> AppResult.Failure(error.toAppError())
}

/** Пользователь сервера → пользователь приложения. */
fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    name = name,
    patronymic = patronymic,
    surname = surname,
    birthday = birthday,
    gender = gender,
    notificationsEnabled = notifications,
)
