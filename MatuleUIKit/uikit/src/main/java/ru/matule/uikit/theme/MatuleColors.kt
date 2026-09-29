package ru.matule.uikit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Цветовая палитра Matule из макета (UI KIT → «Цвета»).
 *
 * @property accent основной цвет: primary-кнопки, активные чипы и вкладки, ссылки, фокус поля, точки PIN.
 * @property accentInactive цвет неактивной primary-кнопки.
 * @property black основной текст и иконки.
 * @property white фон экранов и текст на синих кнопках.
 * @property error обводка и текст ошибки, кнопка «Выход».
 * @property success успешные действия.
 * @property inputBackground фон полей ввода, серых кнопок, неактивных чипов и клавиш PIN.
 * @property inputStroke обводка полей ввода.
 * @property cardStroke обводка карточек и разделители.
 * @property inputIcon иконки в полях и неактивные вкладки таббара.
 * @property placeholder плейсхолдеры и вторичный текст.
 * @property description подписи над полями и описания.
 * @property overlay затемнение под модальными окнами.
 * @property errorBackground фон поля с ошибкой.
 */
@Immutable
data class MatuleColors(
    val accent: Color,
    val accentInactive: Color,
    val black: Color,
    val white: Color,
    val error: Color,
    val success: Color,
    val inputBackground: Color,
    val inputStroke: Color,
    val cardStroke: Color,
    val inputIcon: Color,
    val placeholder: Color,
    val description: Color,
    val overlay: Color,
    val errorBackground: Color,
) {
    companion object {
        /** Светлая палитра — единственная в макете. */
        val Light = MatuleColors(
            accent = Color(0xFF1A6FEE),
            accentInactive = Color(0xFFC9D4FB),
            black = Color(0xFF000000),
            white = Color(0xFFFFFFFF),
            error = Color(0xFFFD3535),
            success = Color(0xFF00B412),
            inputBackground = Color(0xFFF5F5F9),
            inputStroke = Color(0xFFEBEBEB),
            cardStroke = Color(0xFFF4F4F4),
            inputIcon = Color(0xFFB8C1CC),
            placeholder = Color(0xFF939396),
            description = Color(0xFF7E7E9A),
            overlay = Color(0x66000000),
            errorBackground = Color(0x1AFD3535),
        )
    }
}

internal val LocalMatuleColors = staticCompositionLocalOf { MatuleColors.Light }
