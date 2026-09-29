package ru.matule.app.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.matule.app.di.matuleViewModel
import ru.matule.app.presentation.common.ScreenTitle
import ru.matule.app.presentation.common.ScrollableScreen
import ru.matule.uikit.components.button.MatuleButton
import ru.matule.uikit.components.button.MatuleLoginButton
import ru.matule.uikit.components.button.MatuleTextButton
import ru.matule.uikit.components.input.MatuleInput
import ru.matule.uikit.theme.MatuleTheme

/**
 * Экран «Вход и регистрация».
 *
 * @param onSignedIn вход выполнен.
 * @param onRegister переход к регистрации («Создание профиля»).
 */
@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    onRegister: () -> Unit,
) {
    val viewModel = matuleViewModel {
        LoginViewModel(signIn, validateEmail, getLoginEmail, saveLoginEmail, notifier)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnSignedIn by rememberUpdatedState(onSignedIn)
    LaunchedEffect(viewModel) {
        viewModel.signedIn.collect { currentOnSignedIn() }
    }
    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onRegister = onRegister,
        onServiceLogin = viewModel::onServiceLogin,
    )
}

/**
 * Вёрстка экрана «Вход и регистрация».
 *
 * @param state состояние.
 * @param onEmailChange ввод почты.
 * @param onPasswordChange ввод пароля.
 * @param onSubmit «Далее».
 * @param onRegister «Зарегистрироваться».
 * @param onServiceLogin вход через сервис (VK, Yandex).
 */
@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRegister: () -> Unit,
    onServiceLogin: (String) -> Unit,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    val focusManager = LocalFocusManager.current

    ScrollableScreen {
        Spacer(Modifier.height(60.dp))
        ScreenTitle(title = "Добро пожаловать!", emoji = "✋")
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Войдите, чтобы пользоваться функциями приложения",
            style = typography.textRegular,
            color = colors.black,
        )
        Spacer(Modifier.height(64.dp))
        MatuleInput(
            value = state.email,
            onValueChange = onEmailChange,
            label = "Вход по E-mail",
            placeholder = "example@mail.com",
            error = state.emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        MatuleInput(
            value = state.password,
            onValueChange = onPasswordChange,
            label = "Пароль",
            isPassword = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onSubmit()
            }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        MatuleButton(
            text = "Далее",
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            enabled = state.isFilled,
            loading = state.isLoading,
        )
        Spacer(Modifier.height(12.dp))
        MatuleTextButton(
            text = "Зарегистрироваться",
            onClick = onRegister,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Или войдите с помощью",
            style = typography.textRegular,
            color = colors.placeholder,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        MatuleLoginButton(
            text = "Войти с VK",
            onClick = { onServiceLogin("VK") },
            icon = { ServiceBadge(letter = "VK", background = VkColor, content = Color.White) },
        )
        Spacer(Modifier.height(16.dp))
        MatuleLoginButton(
            text = "Войти с Yandex",
            onClick = { onServiceLogin("Yandex") },
            icon = { ServiceBadge(letter = "Я", background = YandexColor, content = Color.Black) },
        )
        Spacer(Modifier.height(20.dp))
    }
}

private val VkColor = Color(0xFF0077FF)
private val YandexColor = Color(0xFFFFCC00)

/**
 * Значок сервиса входа: буква на цветной плашке.
 *
 * @param letter буквы сервиса.
 * @param background цвет плашки.
 * @param content цвет букв.
 */
@Composable
private fun ServiceBadge(letter: String, background: Color, content: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(background, MatuleTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp),
            color = content,
        )
    }
}
