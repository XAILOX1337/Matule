package ru.matule.app.presentation.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.matule.app.di.matuleViewModel
import ru.matule.app.presentation.common.LightStatusBarIcons
import ru.matule.uikit.theme.MatuleTheme
import ru.matule.uikit.theme.SfProDisplay

/** Светло-голубые углы градиента заставки. */
private val SplashLight = Color(0xFF74BFE3)

/** Насыщенный синий в центре заставки. */
private val SplashDeep = Color(0xFF2555F3)

/**
 * Заставка: градиент от светло-голубых углов к синему центру и надпись «Matule».
 *
 * @param onFinished открыть стек экранов после заставки.
 */
@Composable
fun SplashScreen(onFinished: (List<String>) -> Unit) {
    val viewModel = matuleViewModel { SplashViewModel(getStartState) }
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(destination) {
        destination?.let { currentOnFinished(it) }
    }
    LightStatusBarIcons()
    SplashContent()
}

/** Вёрстка заставки. */
@Composable
fun SplashContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Градиент по диагонали: правый верхний угол → центр → левый нижний угол.
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(SplashLight, SplashDeep, SplashLight),
                        start = Offset(size.width, 0f),
                        end = Offset(0f, size.height),
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Matule",
            style = TextStyle(fontFamily = SfProDisplay, fontWeight = FontWeight.Normal, fontSize = 40.sp),
            color = MatuleTheme.colors.white,
        )
    }
}
