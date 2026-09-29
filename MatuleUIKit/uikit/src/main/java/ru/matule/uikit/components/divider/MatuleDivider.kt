package ru.matule.uikit.components.divider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.matule.uikit.theme.MatuleTheme

/**
 * Горизонтальный разделитель (Divider).
 *
 * @param modifier модификатор разделителя.
 * @param color цвет линии.
 * @param thickness толщина линии.
 */
@Composable
fun MatuleDivider(
    modifier: Modifier = Modifier,
    color: Color = MatuleTheme.colors.cardStroke,
    thickness: Dp = 1.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color),
    )
}
