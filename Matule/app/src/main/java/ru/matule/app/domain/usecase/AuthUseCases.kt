package ru.matule.app.domain.usecase

import ru.matule.app.domain.model.ProfileDraft
import ru.matule.app.domain.model.StartState
import ru.matule.app.domain.repository.AppStateRepository
import ru.matule.app.domain.repository.AuthRepository
import ru.matule.app.domain.repository.PinRepository
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.validation.DateValidator
import ru.matule.app.domain.validation.EmailValidator
import ru.matule.app.domain.validation.InputField
import ru.matule.app.domain.validation.PasswordCheck
import ru.matule.app.domain.validation.PasswordValidator

/** Состояние при запуске: есть ли сессия, PIN, последний экран, черновик регистрации. */
class GetStartStateUseCase(
    private val auth: AuthRepository,
    private val pin: PinRepository,
    private val appState: AppStateRepository,
) {
    suspend operator fun invoke(): StartState = StartState(
        hasSession = auth.hasSession(),
        hasPin = pin.hasPin(),
        lastScreen = appState.lastScreen(),
        hasProfileDraft = appState.profileDraft() != null,
    )
}

/** Проверка email по шаблону задания. */
class ValidateEmailUseCase {
    operator fun invoke(email: String): Boolean = EmailValidator.isValid(email.trim())

    companion object {
        /** Текст ошибки неверного email. */
        const val EMAIL_ERROR = "Введите email в формате name@domenname.ru: строчные латинские буквы и цифры"
    }
}

/** Проверка надёжности пароля. */
class ValidatePasswordUseCase {
    operator fun invoke(password: String): PasswordCheck = PasswordValidator.check(password)

    companion object {
        /** Текст ошибки по невыполненным требованиям к паролю. */
        fun errorText(check: PasswordCheck): String = "Пароль должен содержать: ${check.problems.joinToString(", ")}"
    }
}

/** Вход по почте и паролю. */
class SignInUseCase(private val auth: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AppResult<Unit> {
        val login = email.trim()
        if (!EmailValidator.isValid(login)) {
            return AppResult.Failure(AppError.Validation(ValidateEmailUseCase.EMAIL_ERROR, InputField.EMAIL))
        }
        if (password.isEmpty()) return AppResult.Failure(AppError.Validation("Введите пароль", InputField.PASSWORD))
        return auth.signIn(login, password)
    }
}

/**
 * Экран «Создание профиля»: проверяет поля, сохраняет черновик и спрашивает у сервера, свободна ли почта.
 */
class SubmitProfileDraftUseCase(
    private val auth: AuthRepository,
    private val appState: AppStateRepository,
) {
    /**
     * @return успех (можно переходить к созданию пароля) или ошибка с полем.
     */
    suspend operator fun invoke(draft: ProfileDraft): AppResult<Unit> {
        val profile = draft.copy(
            name = draft.name.trim(),
            patronymic = draft.patronymic.trim(),
            surname = draft.surname.trim(),
            email = draft.email.trim(),
        )
        val required = listOf(profile.name, profile.patronymic, profile.surname, profile.gender, profile.email)
        if (required.any { it.isBlank() } || profile.birthdayDigits.isBlank()) {
            return AppResult.Failure(AppError.Validation("Заполните все поля"))
        }
        if (!DateValidator.isValidDigits(profile.birthdayDigits)) {
            return AppResult.Failure(AppError.Validation("Введите существующую дату рождения", InputField.BIRTHDAY))
        }
        if (!EmailValidator.isValid(profile.email)) {
            return AppResult.Failure(AppError.Validation(ValidateEmailUseCase.EMAIL_ERROR, InputField.EMAIL))
        }
        appState.saveProfileDraft(profile)
        return when (val check = auth.isEmailAvailable(profile.email)) {
            is AppResult.Failure -> check
            is AppResult.Success -> if (check.data) {
                AppResult.Success(Unit)
            } else {
                AppResult.Failure(AppError.Validation(EMAIL_TAKEN, InputField.EMAIL))
            }
        }
    }

    companion object {
        /** Почта уже занята. */
        const val EMAIL_TAKEN = "Пользователь с таким email уже зарегистрирован"
    }
}

/** Регистрация: данные из черновика «Создание профиля» + пароль. */
class SignUpUseCase(
    private val auth: AuthRepository,
    private val appState: AppStateRepository,
) {
    suspend operator fun invoke(password: String, passwordRepeat: String): AppResult<Unit> {
        val check = PasswordValidator.check(password)
        if (!check.isValid) {
            return AppResult.Failure(AppError.Validation(ValidatePasswordUseCase.errorText(check), InputField.PASSWORD))
        }
        if (password != passwordRepeat) {
            return AppResult.Failure(AppError.Validation("Пароли не совпадают", InputField.PASSWORD_REPEAT))
        }
        val draft = appState.profileDraft()
            ?: return AppResult.Failure(AppError.Validation("Сначала заполните профиль"))
        val result = auth.signUp(draft, password)
        if (result is AppResult.Success) appState.clearProfileDraft()
        return result
    }
}

/** Обновление токена сохранённой сессии. */
class RefreshSessionUseCase(private val auth: AuthRepository) {
    suspend operator fun invoke(): AppResult<Unit> = auth.refreshSession()
}

/** Выход из аккаунта. */
class LogoutUseCase(private val auth: AuthRepository) {
    suspend operator fun invoke(): AppResult<Unit> = auth.logout()
}

/** Сохранение PIN из 4 цифр. */
class SavePinUseCase(private val pin: PinRepository) {
    suspend operator fun invoke(code: String): AppResult<Unit> {
        if (!PIN_PATTERN.matches(code)) return AppResult.Failure(AppError.Validation("PIN должен состоять из 4 цифр"))
        pin.savePin(code)
        return AppResult.Success(Unit)
    }

    private companion object {
        val PIN_PATTERN = Regex("^\\d{4}$")
    }
}

/** Проверка PIN. */
class VerifyPinUseCase(private val pin: PinRepository) {
    suspend operator fun invoke(code: String): Boolean = pin.verifyPin(code)
}
