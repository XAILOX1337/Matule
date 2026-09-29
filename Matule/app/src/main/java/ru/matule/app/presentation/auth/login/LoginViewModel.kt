package ru.matule.app.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.GetLoginEmailUseCase
import ru.matule.app.domain.usecase.SaveLoginEmailUseCase
import ru.matule.app.domain.usecase.SignInUseCase
import ru.matule.app.domain.usecase.ValidateEmailUseCase
import ru.matule.app.domain.validation.InputField
import ru.matule.app.presentation.common.Notifier

/**
 * Состояние экрана «Вход и регистрация».
 *
 * @property email почта.
 * @property password пароль.
 * @property emailError ошибка под полем почты.
 * @property isLoading идёт запрос.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
) {
    /** Кнопка «Далее» активна, когда оба поля заполнены. */
    val isFilled: Boolean get() = email.isNotBlank() && password.isNotEmpty()
}

/**
 * Экран «Вход и регистрация»: проверка почты, авторизация на сервере.
 *
 * @param signIn вход.
 * @param validateEmail проверка почты.
 * @param getLoginEmail сохранённая почта.
 * @param saveLoginEmail сохранение почты (экран восстанавливается после перезапуска).
 * @param notifier уведомления об ошибках.
 */
class LoginViewModel(
    private val signIn: SignInUseCase,
    private val validateEmail: ValidateEmailUseCase,
    private val getLoginEmail: GetLoginEmailUseCase,
    private val saveLoginEmail: SaveLoginEmailUseCase,
    private val notifier: Notifier,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())

    /** Состояние экрана. */
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val signedInEvents = Channel<Unit>(Channel.BUFFERED)

    /** Вход выполнен — пора создавать PIN. */
    val signedIn: Flow<Unit> = signedInEvents.receiveAsFlow()

    private var saveEmailJob: Job? = null

    init {
        viewModelScope.launch {
            val saved = getLoginEmail()
            if (saved.isNotEmpty()) _state.update { if (it.email.isEmpty()) it.copy(email = saved) else it }
        }
    }

    /** Ввод почты. */
    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailError = null) }
        saveEmailJob?.cancel()
        saveEmailJob = viewModelScope.launch {
            delay(SAVE_DELAY_MS)
            saveLoginEmail(value.trim())
        }
    }

    /** Ввод пароля. */
    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value) }
    }

    /** «Далее». */
    fun submit() {
        val current = _state.value
        if (!current.isFilled || current.isLoading) return
        if (!validateEmail(current.email)) {
            _state.update { it.copy(emailError = ValidateEmailUseCase.EMAIL_ERROR) }
            return
        }
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = signIn(current.email, current.password)
            _state.update { it.copy(isLoading = false) }
            when (result) {
                is AppResult.Success -> signedInEvents.send(Unit)
                is AppResult.Failure -> showError(result.error)
            }
        }
    }

    /** Вход через VK или Яндекс: по заданию только вёрстка. */
    fun onServiceLogin(service: String) {
        notifier.showInfo("Вход через $service пока недоступен", "Войдите по почте и паролю")
    }

    private fun showError(error: AppError) {
        if (error is AppError.Validation && error.field == InputField.EMAIL) {
            _state.update { it.copy(emailError = error.message) }
        } else {
            notifier.showError(error)
        }
    }

    private companion object {
        const val SAVE_DELAY_MS = 300L
    }
}
