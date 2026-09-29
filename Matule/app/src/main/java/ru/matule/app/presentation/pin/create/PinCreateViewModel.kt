package ru.matule.app.presentation.pin.create

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
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.SavePinUseCase
import ru.matule.app.presentation.common.Notifier
import ru.matule.app.presentation.pin.common.PIN_LENGTH

/**
 * Состояние экранов PIN.
 *
 * @property code введённые цифры.
 * @property isError показать ошибку.
 * @property isBusy идёт проверка — ввод заблокирован.
 */
data class PinUiState(
    val code: String = "",
    val isError: Boolean = false,
    val isBusy: Boolean = false,
)

/**
 * Экран «Создайте пароль»: PIN из 4 цифр сохраняется на устройстве (в виде хэша).
 *
 * @param savePin сохранение PIN.
 * @param notifier уведомления об ошибках.
 */
class PinCreateViewModel(
    private val savePin: SavePinUseCase,
    private val notifier: Notifier,
) : ViewModel() {

    private val _state = MutableStateFlow(PinUiState())

    /** Состояние экрана. */
    val state: StateFlow<PinUiState> = _state.asStateFlow()

    private val createdEvents = Channel<Unit>(Channel.BUFFERED)

    /** PIN сохранён. */
    val created: Flow<Unit> = createdEvents.receiveAsFlow()

    /** Нажата цифра. */
    fun onDigit(digit: Int) {
        val current = _state.value
        if (current.isBusy || current.code.length >= PIN_LENGTH) return
        val code = current.code + digit
        _state.value = current.copy(code = code, isError = false)
        if (code.length == PIN_LENGTH) save(code)
    }

    /** Стереть последнюю цифру. */
    fun onBackspace() {
        _state.update { if (it.isBusy) it else it.copy(code = it.code.dropLast(1), isError = false) }
    }

    private fun save(code: String) {
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            when (val result = savePin(code)) {
                is AppResult.Success -> createdEvents.send(Unit)
                is AppResult.Failure -> {
                    notifier.showError(result.error)
                    _state.value = PinUiState(isError = true)
                }
            }
        }
    }
}
