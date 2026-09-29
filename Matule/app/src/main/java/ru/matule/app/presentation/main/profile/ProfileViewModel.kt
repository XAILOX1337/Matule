package ru.matule.app.presentation.main.profile

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
import ru.matule.app.domain.model.User
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.GetCachedProfileUseCase
import ru.matule.app.domain.usecase.GetProfileUseCase
import ru.matule.app.domain.usecase.LogoutUseCase
import ru.matule.app.domain.usecase.SetNotificationsEnabledUseCase
import ru.matule.app.presentation.common.Notifier

/**
 * Состояние экрана «Профиль».
 *
 * @property name имя.
 * @property email почта.
 * @property notificationsEnabled включены ли уведомления.
 * @property isLoggingOut идёт выход.
 */
data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val notificationsEnabled: Boolean = false,
    val isLoggingOut: Boolean = false,
)

/**
 * Экран «Профиль»: данные с сервера (сначала из кэша), настройка уведомлений, выход.
 *
 * @param getCachedProfile профиль из кэша.
 * @param getProfile профиль с сервера.
 * @param setNotificationsEnabled изменение профиля: уведомления.
 * @param logout выход.
 * @param notifier уведомления.
 */
class ProfileViewModel(
    private val getCachedProfile: GetCachedProfileUseCase,
    private val getProfile: GetProfileUseCase,
    private val setNotificationsEnabled: SetNotificationsEnabledUseCase,
    private val logout: LogoutUseCase,
    private val notifier: Notifier,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())

    /** Состояние экрана. */
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val loggedOutEvents = Channel<Unit>(Channel.BUFFERED)

    /** Пользователь вышел — открыть экран входа. */
    val loggedOut: Flow<Unit> = loggedOutEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            getCachedProfile()?.let { show(it) }
            when (val result = getProfile()) {
                is AppResult.Success -> show(result.data)
                is AppResult.Failure -> onFailure(result.error)
            }
        }
    }

    /** Переключатель «Уведомления». */
    fun onNotificationsChange(enabled: Boolean) {
        val previous = _state.value.notificationsEnabled
        _state.update { it.copy(notificationsEnabled = enabled) }
        viewModelScope.launch {
            val result = setNotificationsEnabled(enabled)
            if (result is AppResult.Failure) {
                _state.update { it.copy(notificationsEnabled = previous) }
                onFailure(result.error)
            }
        }
    }

    /** Пользователь запретил уведомления в системном диалоге. */
    fun onNotificationsPermissionDenied() {
        notifier.showInfo("Уведомления запрещены", "Разрешите их в настройках телефона")
    }

    /** «Мои заказы». */
    fun onOrdersClick() {
        notifier.showInfo("Мои заказы", "История заказов появится в следующей версии")
    }

    /** «Выход». */
    fun onLogoutClick() {
        if (_state.value.isLoggingOut) return
        _state.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            val result = logout()
            if (result is AppResult.Failure) {
                notifier.showError(result.error.message, title = "Выход выполнен только на телефоне")
            }
            loggedOutEvents.send(Unit)
        }
    }

    private fun show(user: User) {
        _state.update { it.copy(name = user.name, email = user.email, notificationsEnabled = user.notificationsEnabled) }
    }

    private suspend fun onFailure(error: AppError) {
        notifier.showError(error)
        // Токен отозван или устарел: данные на телефоне очищаются, нужен повторный вход.
        if (error is AppError.Unauthorized) {
            logout()
            loggedOutEvents.send(Unit)
        }
    }
}
