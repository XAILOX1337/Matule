package ru.matule.app.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.matule.app.domain.usecase.GetStartStateUseCase
import ru.matule.app.presentation.navigation.StartRouting

/**
 * Заставка: пока она видна, читает сохранённую сессию и решает, какой экран открыть.
 *
 * @param getStartState состояние при запуске.
 */
class SplashViewModel(getStartState: GetStartStateUseCase) : ViewModel() {

    private val _destination = MutableStateFlow<List<String>?>(null)

    /** Стек экранов, который нужно открыть после заставки; `null` — ещё рано. */
    val destination: StateFlow<List<String>?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val state = async { getStartState() }
            delay(MIN_DURATION_MS)
            _destination.value = StartRouting.afterSplash(state.await())
        }
    }

    private companion object {
        /** Сколько минимум показывать заставку. */
        const val MIN_DURATION_MS = 1_500L
    }
}
