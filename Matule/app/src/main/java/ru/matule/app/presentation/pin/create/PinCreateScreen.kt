package ru.matule.app.presentation.pin.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.matule.app.di.matuleViewModel
import ru.matule.app.presentation.pin.common.PinLayout

/**
 * Экран «Создайте пароль» — PIN для входа в приложение.
 *
 * @param onCreated PIN сохранён.
 */
@Composable
fun PinCreateScreen(onCreated: () -> Unit) {
    val viewModel = matuleViewModel { PinCreateViewModel(savePin, notifier) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnCreated by rememberUpdatedState(onCreated)
    LaunchedEffect(viewModel) {
        viewModel.created.collect { currentOnCreated() }
    }
    PinLayout(
        title = "Создайте пароль",
        subtitle = "Для защиты ваших персональных данных",
        filledCount = state.code.length,
        isError = state.isError,
        enabled = !state.isBusy,
        onDigit = viewModel::onDigit,
        onBackspace = viewModel::onBackspace,
    )
}
