package ru.matule.uikit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.matule.uikit.R

/**
 * Шрифт макета — SF Pro Display. Начертание Bold (700) в наборе отсутствует,
 * поэтому для него Compose подбирает ближайшее — Heavy (800).
 */
val SfProDisplay = FontFamily(
    Font(R.font.sf_pro_display_regular, FontWeight.Normal),
    Font(R.font.sf_pro_display_medium, FontWeight.Medium),
    Font(R.font.sf_pro_display_semibold, FontWeight.SemiBold),
    Font(R.font.sf_pro_display_heavy, FontWeight.ExtraBold),
)

/**
 * Типографика Matule — 16 стилей из макета (UI KIT → «Типографика»).
 * Размеры заданы в `sp`, поэтому текст масштабируется вместе с системным размером шрифта.
 *
 * @property title1Semibold Title 1, 24 sp, Semibold.
 * @property title1ExtraBold Title 1, 24 sp, ExtraBold — крупные заголовки экранов.
 * @property title2Regular Title 2, 20 sp, Regular.
 * @property title2Semibold Title 2, 20 sp, Semibold — заголовки хэдера и модальных окон.
 * @property title2ExtraBold Title 2, 20 sp, ExtraBold — баннеры акций.
 * @property title3Regular Title 3, 17 sp, Regular.
 * @property title3Medium Title 3, 17 sp, Medium.
 * @property title3Semibold Title 3, 17 sp, Semibold — текст больших кнопок, цены.
 * @property headlineRegular Headline, 16 sp, Regular.
 * @property headlineMedium Headline, 16 sp, Medium — названия в карточках.
 * @property textRegular Text, 15 sp, Regular — основной текст и поля ввода.
 * @property textMedium Text, 15 sp, Medium — чипы.
 * @property captionRegular Caption, 14 sp, Regular — подписи полей.
 * @property captionSemibold Caption, 14 sp, Semibold — маленькие кнопки, категории в карточках.
 * @property caption2Regular Caption 2, 12 sp, Regular — подписи таббара.
 * @property caption2Bold Caption 2, 12 sp, Bold.
 */
@Immutable
data class MatuleTypography(
    val title1Semibold: TextStyle,
    val title1ExtraBold: TextStyle,
    val title2Regular: TextStyle,
    val title2Semibold: TextStyle,
    val title2ExtraBold: TextStyle,
    val title3Regular: TextStyle,
    val title3Medium: TextStyle,
    val title3Semibold: TextStyle,
    val headlineRegular: TextStyle,
    val headlineMedium: TextStyle,
    val textRegular: TextStyle,
    val textMedium: TextStyle,
    val captionRegular: TextStyle,
    val captionSemibold: TextStyle,
    val caption2Regular: TextStyle,
    val caption2Bold: TextStyle,
) {
    companion object {
        /** Стили из макета. */
        val Default: MatuleTypography = MatuleTypography(
            title1Semibold = style(24, 28, FontWeight.SemiBold),
            title1ExtraBold = style(24, 28, FontWeight.ExtraBold),
            title2Regular = style(20, 28, FontWeight.Normal),
            title2Semibold = style(20, 28, FontWeight.SemiBold),
            title2ExtraBold = style(20, 28, FontWeight.ExtraBold),
            title3Regular = style(17, 24, FontWeight.Normal),
            title3Medium = style(17, 24, FontWeight.Medium),
            title3Semibold = style(17, 24, FontWeight.SemiBold),
            headlineRegular = style(16, 20, FontWeight.Normal),
            headlineMedium = style(16, 20, FontWeight.Medium),
            textRegular = style(15, 20, FontWeight.Normal),
            textMedium = style(15, 20, FontWeight.Medium),
            captionRegular = style(14, 20, FontWeight.Normal),
            captionSemibold = style(14, 20, FontWeight.SemiBold),
            caption2Regular = style(12, 16, FontWeight.Normal),
            caption2Bold = style(12, 16, FontWeight.Bold),
        )

        private fun style(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
            fontFamily = SfProDisplay,
            fontSize = size.sp,
            lineHeight = lineHeight.sp,
            fontWeight = weight,
        )
    }
}

internal val LocalMatuleTypography = staticCompositionLocalOf { MatuleTypography.Default }
