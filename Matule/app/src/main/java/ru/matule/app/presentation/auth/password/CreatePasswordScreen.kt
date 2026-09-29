package ru.matule.app.presentation.auth.password

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.matule.app.di.matuleViewModel
import ru.matule.app.presentation.common.ScreenTitle
import ru.matule.app.presentation.common.ScrollableScreen
import ru.matule.uikit.components.button.MatuleButton
import ru.matule.uikit.components.input.MatuleInput
import ru.matule.uikit.theme.MatuleTheme

/**
 * Экран «Создание пароля» профиля.
 *
 * @param onRegistered регистрация завершена.
 */
@Composable
fun CreatePasswordScreen(onRegistered: () -> Unit) {
    val viewModel = matuleViewModel { CreatePasswordViewModel(signUp, validatePassword, notifier) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnRegistered by rememberUpdatedState(onRegistered)
    LaunchedEffect(viewModel) {
        viewModel.registered.collect { currentOnRegistered() }
    }
    CreatePasswordContent(
        state = state,
        onPasswordChange = viewModel::onPasswordChange,
        onRepeatChange = viewModel::onRepeatChange,
        onSubmit = viewModel::submit,
    )
}

/**
 * Вёрстка экрана «Создание пароля».
 *
 * @param state состояние.
 * @param onPasswordChange ввод пароля.
 * @param onRepeatChange ввод повтора.
 * @param onSubmit «Сохранить».
 */
@Composable
fun CreatePasswordContent(
    state: CreatePasswordUiState,
    onPasswordChange: (String) -> Unit,
    onRepeatChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    ScrollableScreen {
        Spacer(Modifier.height(60.dp))
        ScreenTitle(title = "Создание пароля", emoji = "✋")
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Введите новый пароль",
            style = MatuleTheme.typography.textRegular,
            color = MatuleTheme.colors.black,
        )
        Spacer(Modifier.height(88.dp))
        MatuleInput(
            value = state.password,
            onValueChange = onPasswordChange,
            label = "Новый Пароль",
            isPassword = true,
            error = state.passwordError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        MatuleInput(
            value = state.repeat,
            onValueChange = onRepeatChange,
            label = "Повторите пароль",
            isPassword = true,
            error = state.repeatError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onSubmit()
            }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        MatuleButton(
            text = "Сохранить",
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            enabled = state.isFilled,
            loading = state.isLoading,
        )
        Spacer(Modifier.height(20.dp))
    }
}
