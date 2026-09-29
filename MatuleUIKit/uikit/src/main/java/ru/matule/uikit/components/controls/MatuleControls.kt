package ru.matule.uikit.components.controls

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/**
 * Переключатель (Контролы → Toggle): включённый — синий, выключенный — серый.
 *
 * @param checked включён ли переключатель.
 * @param onCheckedChange вызывается с новым значением при нажатии.
 * @param modifier модификатор переключателя.
 * @param enabled можно ли переключать.
 */
@Composable
fun MatuleToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MatuleTheme.colors
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.inputBackground
            checked -> colors.accent
            else -> colors.inputStroke
        },
        label = "toggleTrack",
    )
    val thumbOffset by animateDpAsState(targetValue = if (checked) 20.dp else 0.dp, label = "toggleThumb")
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(MatuleTheme.shapes.circle)
            .background(trackColor)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .shadow(elevation = 2.dp, shape = MatuleTheme.shapes.circle)
                .clip(MatuleTheme.shapes.circle)
                .background(colors.white),
        )
    }
}

/**
 * Счётчик количества (Контролы → Counter): «−» и «+» с разделителем.
 * Кнопка становится бледной, когда достигнута граница.
 *
 * @param count текущее значение.
 * @param onIncrement нажатие «+».
 * @param onDecrement нажатие «−».
 * @param modifier модификатор счётчика.
 * @param minCount минимальное значение (при нём «−» неактивен).
 * @param maxCount максимальное значение (при нём «+» неактивен).
 */
@Composable
fun MatuleCounter(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    minCount: Int = 1,
    maxCount: Int = 99,
) {
    val colors = MatuleTheme.colors
    val canDecrement = count > minCount
    val canIncrement = count < maxCount
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(MatuleTheme.shapes.small)
            .background(colors.inputBackground)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MatuleIcon(
            icon = MatuleIcons.Minus,
            size = 20.dp,
            tint = if (canDecrement) colors.black else colors.inputIcon,
            contentDescription = "Уменьшить",
            modifier = Modifier
                .clip(MatuleTheme.shapes.circle)
                .clickable(enabled = canDecrement, onClick = onDecrement),
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .width(1.dp)
                .height(16.dp)
                .background(colors.inputStroke),
        )
        MatuleIcon(
            icon = MatuleIcons.Plus,
            size = 20.dp,
            tint = if (canIncrement) colors.black else colors.inputIcon,
            contentDescription = "Увеличить",
            modifier = Modifier
                .clip(MatuleTheme.shapes.circle)
                .clickable(enabled = canIncrement, onClick = onIncrement),
        )
    }
}
