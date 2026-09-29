package ru.matule.app.presentation.pin.enter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.GetLastScreenUseCase
import ru.matule.app.domain.usecase.LogoutUseCase
import ru.matule.app.domain.usecase.RefreshSessionUseCase
import ru.matule.app.domain.usecase.VerifyPinUseCase
import ru.matule.app.presentation.common.Notifier
import ru.matule.app.presentation.navigation.StartRouting
import ru.matule.app.presentation.pin.common.PIN_LENGTH
import ru.matule.app.presentation.pin.create.PinUiState

/** Результат экрана входа по PIN. */
sealed interface PinEnterEvent {
    /**
     * Вход выполнен.
     *
     * @property backStack стек экранов: последний открытый экран и экраны под ним.
     */
    data class Unlocked(val backStack: List<String>) : PinEnterEvent

    /** Сессия недействительна или слишком много ошибок — нужен повторный вход. */
    data object LoggedOut : PinEnterEvent
}

/**
 * Экран «Вход по паролю или биометрии». После входа обновляет токен на сервере
 * и открывает последний экран, на котором пользователь закрыл приложение.
 *
 * @param verifyPin проверка PIN.
 * @param refreshSession обновление токена.
 * @param getLastScreen последний открытый экран.
 * @param logout выход при недействительной сессии.
 * @param notifier уведомления.
 */
class PinEnterViewModel(
    private val verifyPin: VerifyPinUseCase,
    private val refreshSession: RefreshSessionUseCase,
    private val getLastScreen: GetLastScreenUseCase,
    private val logout: LogoutUseCase,
    private val notifier: Notifier,
) : ViewModel() {

    private val _state = MutableStateFlow(PinUiState())

    /** Состояние экрана. */
    val state: StateFlow<PinUiState> = _state.asStateFlow()

    private val eventChannel = Channel<PinEnterEvent>(Channel.BUFFERED)

    /** Результат входа. */
    val events: Flow<PinEnterEvent> = eventChannel.receiveAsFlow()

    private var failedAttempts = 0

    /** Нажата цифра. */
    fun onDigit(digit: Int) {
        val current = _state.value
        if (current.isBusy || current.code.length >= PIN_LENGTH) return
        val code = current.code + digit
        _state.value = current.copy(code = code, isError = false)
        if (code.length == PIN_LENGTH) check(code)
    }

    /** Стереть последнюю цифру. */
    fun onBackspace() {
        _state.update { if (it.isBusy) it else it.copy(code = it.code.dropLast(1), isError = false) }
    }

    /** Биометрия подтверждена. */
    fun onBiometricSuccess() {
        if (_state.value.isBusy) return
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch { unlock() }
    }

    private fun check(code: String) {
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            if (verifyPin(code)) {
                unlock()
                return@launch
            }
            failedAttempts++
            _state.update { it.copy(isError = true) }
            delay(ERROR_DELAY_MS)
            val attemptsLeft = MAX_ATTEMPTS - failedAttempts
            if (attemptsLeft <= 0) {
                signOut("Слишком много неверных попыток. Войдите по почте и паролю")
            } else {
                notifier.showError("Неверный пароль. Осталось попыток: $attemptsLeft")
                _state.value = PinUiState()
            }
        }
    }

    private suspend fun unlock() {
        // Продлеваем сессию, но не держим пользователя дольше пары секунд, если сервер не отвечает.
        val refresh = withTimeoutOrNull(REFRESH_TIMEOUT_MS) { refreshSession() }
        if (refresh is AppResult.Failure && refresh.error is AppError.Unauthorized) {
            signOut(refresh.error.message)
            return
        }
        eventChannel.send(PinEnterEvent.Unlocked(StartRouting.mainBackStack(getLastScreen())))
    }

    private suspend fun signOut(message: String) {
        logout()
        notifier.showError(message)
        eventChannel.send(PinEnterEvent.LoggedOut)
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
        const val ERROR_DELAY_MS = 600L
        const val REFRESH_TIMEOUT_MS = 3_000L
    }
}
