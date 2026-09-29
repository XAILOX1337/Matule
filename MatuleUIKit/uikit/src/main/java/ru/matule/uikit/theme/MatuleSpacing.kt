package ru.matule.uikit.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Шкала отступов из макета (UI KIT → «Отступы»): 4 · 8 · 12 · 16 · 20 · 24 · 32 · 40 · 48.
 *
 * @property xxs 4 dp.
 * @property xs 8 dp.
 * @property s 12 dp.
 * @property m 16 dp.
 * @property l 20 dp.
 * @property xl 24 dp.
 * @property xxl 32 dp.
 * @property xxxl 40 dp.
 * @property huge 48 dp.
 * @property screen горизонтальный отступ контента от края экрана (20 dp).
 */
@Immutable
data class MatuleSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val s: Dp = 12.dp,
    val m: Dp = 16.dp,
    val l: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 40.dp,
    val huge: Dp = 48.dp,
    val screen: Dp = 20.dp,
)

/**
 * Скругления компонентов.
 *
 * @property small маленькие кнопки и счётчик.
 * @property medium поля ввода, большие кнопки, чипы.
 * @property large карточки и баннеры.
 * @property sheet верхние углы BottomSheet.
 * @property circle круглые элементы: bubble-кнопки, клавиши PIN.
 */
@Immutable
data class MatuleShapes(
    val small: Shape = RoundedCornerShape(8.dp),
    val medium: Shape = RoundedCornerShape(10.dp),
    val large: Shape = RoundedCornerShape(12.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    val circle: Shape = CircleShape,
)

/**
 * Размеры компонентов, замеренные по макету.
 */
object MatuleSizes {
    /** Высота поля ввода, селекта и поиска. */
    val inputHeight: Dp = 48.dp

    /** Высота большой кнопки. */
    val bigButtonHeight: Dp = 56.dp

    /** Высота маленькой кнопки. */
    val smallButtonHeight: Dp = 32.dp

    /** Высота чипа. */
    val chipHeight: Dp = 48.dp

    /** Диаметр клавиши PIN-клавиатуры. */
    val pinKey: Dp = 80.dp

    /** Диаметр bubble-кнопки. */
    val bubble: Dp = 32.dp
}

internal val LocalMatuleSpacing = staticCompositionLocalOf { MatuleSpacing() }
internal val LocalMatuleShapes = staticCompositionLocalOf { MatuleShapes() }
