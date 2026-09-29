package ru.matule.app.presentation.auth.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.SignUpUseCase
import ru.matule.app.domain.usecase.ValidatePasswordUseCase
import ru.matule.app.domain.validation.InputField
import ru.matule.app.presentation.common.Notifier

/**
 * Состояние экрана «Создание пароля».
 *
 * @property password новый пароль.
 * @property repeat повтор пароля.
 * @property passwordError какие требования к паролю не выполнены.
 * @property repeatError ошибка повтора.
 * @property attempted уже нажимали «Сохранить» — ошибки пересчитываются при вводе.
 * @property isLoading идёт регистрация.
 */
data class CreatePasswordUiState(
    val password: String = "",
    val repeat: String = "",
    val passwordError: String? = null,
    val repeatError: String? = null,
    val attempted: Boolean = false,
    val isLoading: Boolean = false,
) {
    /** «Сохранить» активна, когда оба поля заполнены. */
    val isFilled: Boolean get() = password.isNotEmpty() && repeat.isNotEmpty()
}

/**
 * Экран «Создание пароля»: проверка надёжности, совпадение паролей и регистрация на сервере.
 *
 * @param signUp регистрация и вход.
 * @param validatePassword проверка требований к паролю.
 * @param notifier уведомления об ошибках.
 */
class CreatePasswordViewModel(
    private val signUp: SignUpUseCase,
    private val validatePassword: ValidatePasswordUseCase,
    private val notifier: Notifier,
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePasswordUiState())

    /** Состояние экрана. */
    val state: StateFlow<CreatePasswordUiState> = _state.asStateFlow()

    private val registeredEvents = Channel<Unit>(Channel.BUFFERED)

    /** Пользователь зарегистрирован и вошёл — пора создавать PIN. */
    val registered: Flow<Unit> = registeredEvents.receiveAsFlow()

    /** Ввод пароля. После первой попытки ошибка обновляется на лету. */
    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, passwordError = if (it.attempted) passwordProblems(value) else null) }
    }

    /** Ввод повтора. */
    fun onRepeatChange(value: String) {
        _state.update { it.copy(repeat = value, repeatError = null) }
    }

    /** «Сохранить». */
    fun submit() {
        val current = _state.value
        if (!current.isFilled || current.isLoading) return
        val passwordError = passwordProblems(current.password)
        val repeatError = if (current.password != current.repeat) MISMATCH else null
        _state.update { it.copy(passwordError = passwordError, repeatError = repeatError, attempted = true) }
        if (passwordError != null || repeatError != null) return

        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = signUp(current.password, current.repeat)
            _state.update { it.copy(isLoading = false) }
            when (result) {
                is AppResult.Success -> registeredEvents.send(Unit)
                is AppResult.Failure -> showError(result.error)
            }
        }
    }

    private fun passwordProblems(password: String): String? {
        val check = validatePassword(password)
        return if (check.isValid) null else ValidatePasswordUseCase.errorText(check)
    }

    private fun showError(error: AppError) {
        val field = (error as? AppError.Validation)?.field
        when (field) {
            InputField.PASSWORD -> _state.update { it.copy(passwordError = error.message) }
            InputField.PASSWORD_REPEAT -> _state.update { it.copy(repeatError = error.message) }
            else -> notifier.showError(error)
        }
    }

    private companion object {
        const val MISMATCH = "Пароли не совпадают"
    }
}
