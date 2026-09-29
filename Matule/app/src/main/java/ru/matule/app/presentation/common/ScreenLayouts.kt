package ru.matule.app.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.matule.uikit.theme.MatuleTheme

/**
 * Каркас экрана с прокруткой. На маленьком экране и при крупном шрифте контент прокручивается,
 * а на большом `Spacer(Modifier.weight(1f))` внутри [content] прижимает нижнюю часть к низу экрана,
 * не оставляя пустоты посередине. Учитывает системные панели и клавиатуру.
 *
 * @param modifier модификатор корня.
 * @param horizontalPadding боковые отступы контента.
 * @param windowInsets какие системные отступы учитывать.
 * @param horizontalAlignment выравнивание элементов по горизонтали.
 * @param content содержимое экрана.
 */
@Composable
fun ScrollableScreen(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
    windowInsets: WindowInsets = WindowInsets.safeDrawing,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MatuleTheme.colors.white)
            .windowInsetsPadding(windowInsets),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = horizontalPadding),
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}

/**
 * Крупный заголовок экрана, при необходимости со смайликом слева («✋ Добро пожаловать!»).
 *
 * @param title текст заголовка.
 * @param modifier модификатор.
 * @param emoji смайлик перед заголовком или `null`.
 */
@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
) {
    val style = MatuleTheme.typography.title1ExtraBold
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (emoji != null) {
            Text(text = emoji, style = style)
            Spacer(Modifier.width(12.dp))
        }
        Text(text = title, style = style, color = MatuleTheme.colors.black)
    }
}
