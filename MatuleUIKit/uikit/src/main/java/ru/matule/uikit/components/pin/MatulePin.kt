package ru.matule.uikit.components.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleSizes
import ru.matule.uikit.theme.MatuleTheme

/**
 * Индикатор ввода PIN: ряд точек, заполненные — синие, при ошибке — красные.
 *
 * @param filledCount сколько цифр уже введено.
 * @param modifier модификатор ряда.
 * @param length длина PIN.
 * @param isError показать состояние ошибки.
 */
@Composable
fun MatulePinDots(
    filledCount: Int,
    modifier: Modifier = Modifier,
    length: Int = 4,
    isError: Boolean = false,
) {
    val color = if (isError) MatuleTheme.colors.error else MatuleTheme.colors.accent
    val shape = MatuleTheme.shapes.circle
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(length) { index ->
            val dotModifier = if (index < filledCount) {
                Modifier.background(color, shape)
            } else {
                Modifier.border(1.dp, color, shape)
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(shape)
                    .then(dotModifier),
            )
        }
    }
}

/**
 * Цифровая клавиатура для PIN: 1–9, 0 и «стереть». Нажатая клавиша подсвечивается синим, как в макете.
 * Размер клавиш уменьшается, если клавиатура не помещается в отведённое место.
 *
 * @param onDigit вызывается с нажатой цифрой.
 * @param onBackspace нажатие «стереть».
 * @param modifier модификатор клавиатуры.
 * @param bottomStartContent содержимое свободной ячейки слева от 0 (например, вход по биометрии); `null` — пусто.
 * @param maxKeySize максимальный диаметр клавиши.
 * @param spacing расстояние между клавишами.
 */
@Composable
fun MatuleNumPad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    bottomStartContent: (@Composable () -> Unit)? = null,
    maxKeySize: Dp = MatuleSizes.pinKey,
    spacing: Dp = 24.dp,
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val byWidth = (maxWidth - spacing * 2) / 3
        val byHeight = (maxHeight - spacing * 3) / 4
        val keySize = min(maxKeySize, min(byWidth, byHeight)).coerceAtLeast(40.dp)
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    row.forEach { digit -> PinKey(text = digit.toString(), size = keySize) { onDigit(digit) } }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(keySize), contentAlignment = Alignment.Center) {
                    bottomStartContent?.invoke()
                }
                PinKey(text = "0", size = keySize) { onDigit(0) }
                Box(
                    modifier = Modifier
                        .size(keySize)
                        .clip(MatuleTheme.shapes.circle)
                        .clickable(role = Role.Button, onClick = onBackspace),
                    contentAlignment = Alignment.Center,
                ) {
                    MatuleIcon(
                        icon = MatuleIcons.Backspace,
                        size = 32.dp,
                        tint = MatuleTheme.colors.black,
                        contentDescription = "Стереть",
                    )
                }
            }
        }
    }
}

/**
 * Клавиша цифры.
 *
 * @param text цифра.
 * @param size диаметр клавиши.
 * @param onClick обработчик нажатия.
 */
@Composable
private fun PinKey(text: String, size: Dp, onClick: () -> Unit) {
    val colors = MatuleTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .size(size)
            .clip(MatuleTheme.shapes.circle)
            .background(if (pressed) colors.accent else colors.inputBackground)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MatuleTheme.typography.title1Semibold,
            color = if (pressed) colors.white else colors.black,
        )
    }
}
