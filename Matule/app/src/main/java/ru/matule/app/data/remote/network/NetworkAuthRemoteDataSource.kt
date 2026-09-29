package ru.matule.app.data.remote.network

import ru.matule.app.data.remote.AuthRemoteDataSource
import ru.matule.app.data.remote.RemoteSession
import ru.matule.app.domain.model.ProfileDraft
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.validation.DateValidator
import ru.matule.network.MatuleApi
import ru.matule.network.model.SignUpRequestDto
import ru.matule.network.result.NetworkError
import ru.matule.network.result.NetworkResult

/**
 * Авторизация через библиотеку MatuleNetwork.
 *
 * @param api клиент сервера.
 */
class NetworkAuthRemoteDataSource(private val api: MatuleApi) : AuthRemoteDataSource {

    override suspend fun signIn(email: String, password: String): AppResult<RemoteSession> =
        when (val result = api.auth.signIn(email, password)) {
            is NetworkResult.Success -> AppResult.Success(RemoteSession(result.data.token, result.data.record.toDomain()))
            // PocketBase отвечает 400 без подробностей, если почта или пароль неверны.
            is NetworkResult.Failure -> AppResult.Failure(
                if (result.error is NetworkError.Validation) AppError.Validation(WRONG_CREDENTIALS) else result.error.toAppError(),
            )
        }

    override suspend fun isEmailAvailable(email: String): AppResult<Boolean> =
        api.auth.isEmailAvailable(email).toAppResult { it }

    override suspend fun signUp(draft: ProfileDraft, password: String): AppResult<Unit> =
        api.auth.signUp(
            SignUpRequestDto(
                email = draft.email,
                password = password,
                passwordConfirm = password,
                name = draft.name,
                patronymic = draft.patronymic,
                surname = draft.surname,
                birthday = DateValidator.format(draft.birthdayDigits),
                gender = draft.gender,
            ),
        ).toAppResult { }

    override suspend fun refresh(): AppResult<RemoteSession> =
        api.auth.refresh().toAppResult { RemoteSession(it.token, it.record.toDomain()) }

    override suspend fun logout(): AppResult<Unit> = api.auth.logout().toAppResult { }

    private companion object {
        const val WRONG_CREDENTIALS = "Неверный email или пароль"
    }
}
