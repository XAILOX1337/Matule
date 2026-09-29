package ru.matule.app.presentation.pin.enter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import ru.matule.app.di.matuleViewModel
import ru.matule.app.presentation.common.findActivity
import ru.matule.app.presentation.pin.common.PinLayout

/**
 * Экран «Вход по паролю или биометрии». Вёрстка как в макете, а системный диалог биометрии
 * открывается сам, если на телефоне настроен отпечаток или лицо.
 *
 * @param onUnlocked вход выполнен — открыть стек экранов.
 * @param onLoggedOut нужна повторная авторизация.
 */
@Composable
fun PinEnterScreen(
    onUnlocked: (List<String>) -> Unit,
    onLoggedOut: () -> Unit,
) {
    val viewModel = matuleViewModel {
        PinEnterViewModel(verifyPin, refreshSession, getLastScreen, logout, notifier)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnUnlocked by rememberUpdatedState(onUnlocked)
    val currentOnLoggedOut by rememberUpdatedState(onLoggedOut)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PinEnterEvent.Unlocked -> currentOnUnlocked(event.backStack)
                PinEnterEvent.LoggedOut -> currentOnLoggedOut()
            }
        }
    }

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() as? FragmentActivity }
    var biometricRequested by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(activity) {
        if (activity != null && !biometricRequested && BiometricAuth.isAvailable(activity)) {
            biometricRequested = true
            delay(BIOMETRIC_DELAY_MS)
            BiometricAuth.authenticate(activity, onSuccess = viewModel::onBiometricSuccess)
        }
    }

    PinLayout(
        title = "Вход",
        subtitle = null,
        filledCount = state.code.length,
        isError = state.isError,
        enabled = !state.isBusy,
        onDigit = viewModel::onDigit,
        onBackspace = viewModel::onBackspace,
    )
}

/** Пауза перед показом биометрии, чтобы экран успел появиться. */
private const val BIOMETRIC_DELAY_MS = 300L
