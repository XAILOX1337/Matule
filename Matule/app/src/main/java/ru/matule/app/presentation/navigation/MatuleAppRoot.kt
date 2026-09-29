package ru.matule.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import ru.matule.app.presentation.common.Notifier
import ru.matule.uikit.components.snackbar.MatuleSnackBarHost
import ru.matule.uikit.components.snackbar.rememberMatuleSnackBarState
import ru.matule.uikit.theme.MatuleTheme

/**
 * Корень интерфейса: навигация по экранам и общий SnackBar для ошибок и уведомлений
 * поверх любого экрана.
 *
 * @param notifier источник уведомлений.
 */
@Composable
fun MatuleAppRoot(notifier: Notifier) {
    val navController = rememberNavController()
    val snackBarState = rememberMatuleSnackBarState()

    LaunchedEffect(notifier) {
        notifier.messages.collect { message ->
            snackBarState.show(message.title, message.text, message.durationMillis)
        }
    }
    LastScreenSaver(navController)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatuleTheme.colors.white),
    ) {
        MatuleNavHost(navController = navController)
        MatuleSnackBarHost(
            state = snackBarState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        )
    }
}
