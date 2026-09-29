package ru.matule.app.presentation.main.profile

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.matule.app.di.matuleViewModel
import ru.matule.app.presentation.common.hasNotificationPermission
import ru.matule.app.presentation.main.common.MainTab
import ru.matule.app.presentation.main.common.MainTabScaffold
import ru.matule.uikit.components.button.MatuleTextButton
import ru.matule.uikit.components.controls.MatuleToggle
import ru.matule.uikit.components.misc.MatuleMenuItem
import ru.matule.uikit.theme.MatuleTheme

/**
 * Экран «Профиль».
 *
 * @param onTabSelected переход на другую вкладку.
 * @param onOpenPrivacyPolicy «Политика конфиденциальности».
 * @param onOpenUserAgreement «Пользовательское соглашение».
 * @param onLoggedOut выход выполнен.
 */
@Composable
fun ProfileScreen(
    onTabSelected: (MainTab) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenUserAgreement: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val viewModel = matuleViewModel {
        ProfileViewModel(getCachedProfile, getProfile, setNotificationsEnabled, logout, notifier)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnLoggedOut by rememberUpdatedState(onLoggedOut)
    LaunchedEffect(viewModel) {
        viewModel.loggedOut.collect { currentOnLoggedOut() }
    }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.onNotificationsChange(true) else viewModel.onNotificationsPermissionDenied()
    }

    MainTabScaffold(selected = MainTab.Profile, onTabSelected = onTabSelected) {
        ProfileContent(
            state = state,
            onNotificationsChange = { enabled ->
                if (enabled && !context.hasNotificationPermission()) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onNotificationsChange(enabled)
                }
            },
            onOrdersClick = viewModel::onOrdersClick,
            onPrivacyPolicyClick = onOpenPrivacyPolicy,
            onUserAgreementClick = onOpenUserAgreement,
            onLogoutClick = viewModel::onLogoutClick,
        )
    }
}

/**
 * Вёрстка экрана «Профиль» (без нижнего меню).
 *
 * @param state состояние.
 * @param onNotificationsChange переключатель уведомлений.
 * @param onOrdersClick «Мои заказы».
 * @param onPrivacyPolicyClick «Политика конфиденциальности».
 * @param onUserAgreementClick «Пользовательское соглашение».
 * @param onLogoutClick «Выход».
 */
@Composable
fun ProfileContent(
    state: ProfileUiState,
    onNotificationsChange: (Boolean) -> Unit,
    onOrdersClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onUserAgreementClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                text = state.name,
                style = typography.title1ExtraBold,
                color = colors.black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Text(text = state.email, style = typography.captionRegular, color = colors.placeholder)
            Spacer(Modifier.height(32.dp))
            MatuleMenuItem(title = "Мои заказы", emoji = "📋", onClick = onOrdersClick)
            Spacer(Modifier.height(16.dp))
            MatuleMenuItem(
                title = "Уведомления",
                emoji = "⚙️",
                trailing = {
                    MatuleToggle(checked = state.notificationsEnabled, onCheckedChange = onNotificationsChange)
                },
            )

            // Ссылки стоят посередине свободного места, как в макете.
            Spacer(Modifier.height(40.dp))
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MatuleTextButton(
                    text = "Политика конфиденциальности",
                    onClick = onPrivacyPolicyClick,
                    color = colors.placeholder,
                )
                Spacer(Modifier.height(12.dp))
                MatuleTextButton(
                    text = "Пользовательское соглашение",
                    onClick = onUserAgreementClick,
                    color = colors.placeholder,
                )
                Spacer(Modifier.height(12.dp))
                MatuleTextButton(text = "Выход", onClick = onLogoutClick, color = colors.error)
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
        }
    }
}
