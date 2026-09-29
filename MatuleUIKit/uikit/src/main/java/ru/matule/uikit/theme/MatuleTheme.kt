package ru.matule.uikit.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Доступ к токенам темы Matule внутри композиции: `MatuleTheme.colors.accent`,
 * `MatuleTheme.typography.textRegular`, `MatuleTheme.spacing.screen`, `MatuleTheme.shapes.medium`.
 */
object MatuleTheme {
    /** Текущая палитра. */
    val colors: MatuleColors
        @Composable @ReadOnlyComposable get() = LocalMatuleColors.current

    /** Текущая типографика. */
    val typography: MatuleTypography
        @Composable @ReadOnlyComposable get() = LocalMatuleTypography.current

    /** Текущая шкала отступов. */
    val spacing: MatuleSpacing
        @Composable @ReadOnlyComposable get() = LocalMatuleSpacing.current

    /** Текущие скругления. */
    val shapes: MatuleShapes
        @Composable @ReadOnlyComposable get() = LocalMatuleShapes.current
}

/**
 * Тема Matule. Оборачивает контент в токены UIKit и в MaterialTheme с той же палитрой,
 * чтобы системные компоненты (например, BottomSheet) выглядели согласованно.
 *
 * @param colors палитра; по умолчанию палитра из макета.
 * @param typography типографика; по умолчанию стили из макета.
 * @param content содержимое экрана.
 */
@Composable
fun MatuleTheme(
    colors: MatuleColors = MatuleColors.Light,
    typography: MatuleTypography = MatuleTypography.Default,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMatuleColors provides colors,
        LocalMatuleTypography provides typography,
        LocalMatuleSpacing provides MatuleSpacing(),
        LocalMatuleShapes provides MatuleShapes(),
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = colors.accent,
                onPrimary = colors.white,
                secondary = colors.accentInactive,
                background = colors.white,
                onBackground = colors.black,
                surface = colors.white,
                onSurface = colors.black,
                surfaceVariant = colors.inputBackground,
                onSurfaceVariant = colors.description,
                outline = colors.inputStroke,
                error = colors.error,
                onError = colors.white,
            ),
        ) {
            ProvideTextStyle(value = typography.textRegular.copy(color = colors.black), content = content)
        }
    }
}
