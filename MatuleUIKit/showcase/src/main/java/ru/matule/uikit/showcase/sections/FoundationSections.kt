package ru.matule.uikit.showcase.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.misc.MatuleLogo
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.showcase.ShowcaseSection
import ru.matule.uikit.showcase.ShowcaseState
import ru.matule.uikit.theme.MatuleTheme

/** Логотип. */
@Composable
fun LogoSection() {
    ShowcaseSection("Логотип") {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            MatuleLogo(size = 96.dp)
            MatuleLogo(size = 48.dp)
            MatuleLogo(size = 24.dp)
        }
    }
}

/** Цвета темы с названиями из макета. */
@Composable
fun ColorsSection() {
    val c = MatuleTheme.colors
    val swatches = listOf(
        "Accent" to c.accent,
        "Accent inactive" to c.accentInactive,
        "Black" to c.black,
        "White" to c.white,
        "Error" to c.error,
        "Success" to c.success,
        "Инпут бг" to c.inputBackground,
        "Инпут строк" to c.inputStroke,
        "Кард строк" to c.cardStroke,
        "Инпут иконки" to c.inputIcon,
        "Плейсхолдер" to c.placeholder,
        "Дескрипшн" to c.description,
    )
    ShowcaseSection("Цвета") {
        swatches.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { (name, color) -> Swatch(name, color, Modifier.weight(1f)) }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Swatch(name: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color, CircleShape)
                .border(1.dp, MatuleTheme.colors.cardStroke, CircleShape),
        )
        Spacer(Modifier.height(6.dp))
        Text(text = name, style = MatuleTheme.typography.caption2Regular, textAlign = TextAlign.Center)
        Text(
            text = "#%06X".format(color.toArgb() and 0xFFFFFF),
            style = MatuleTheme.typography.caption2Regular,
            color = MatuleTheme.colors.placeholder,
        )
    }
}

/** Все стили типографики. */
@Composable
fun TypographySection() {
    val t = MatuleTheme.typography
    val styles = listOf(
        "Title 1 - Semibold" to t.title1Semibold,
        "Title 1 - ExtraBold" to t.title1ExtraBold,
        "Title 2 - Regular" to t.title2Regular,
        "Title 2 - Semibold" to t.title2Semibold,
        "Title 2 - ExtraBold" to t.title2ExtraBold,
        "Title 3 - Regular" to t.title3Regular,
        "Title 3 - Medium" to t.title3Medium,
        "Title 3 - Semibold" to t.title3Semibold,
        "Headline - Regular" to t.headlineRegular,
        "Headline - Medium" to t.headlineMedium,
        "Text - Regular" to t.textRegular,
        "Text - Medium" to t.textMedium,
        "Caption - Regular" to t.captionRegular,
        "Caption - Semibold" to t.captionSemibold,
        "Caption 2 - Regular" to t.caption2Regular,
        "Caption 2 - Bold" to t.caption2Bold,
    )
    ShowcaseSection("Типографика") {
        styles.forEach { (name, style) ->
            Text(text = "$name · ${style.fontSize.value.toInt()} sp", style = style, color = MatuleTheme.colors.black)
        }
    }
}

/** Иконочный шрифт: все иконки с названиями. */
@Composable
fun IconsSection() {
    ShowcaseSection("Иконки") {
        ShowcaseState("Шрифт matule_icons.ttf, собран из иконок макета") {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MatuleIcons.entries.chunked(4).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { icon ->
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                MatuleIcon(icon = icon, size = 28.dp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = icon.title,
                                    style = MatuleTheme.typography.caption2Regular,
                                    color = MatuleTheme.colors.description,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        ShowcaseState("Размер и цвет задаются параметрами") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                MatuleIcon(icon = MatuleIcons.Cart, size = 16.dp, tint = MatuleTheme.colors.inputIcon)
                MatuleIcon(icon = MatuleIcons.Cart, size = 24.dp, tint = MatuleTheme.colors.accent)
                MatuleIcon(icon = MatuleIcons.Cart, size = 32.dp, tint = MatuleTheme.colors.error)
                MatuleIcon(icon = MatuleIcons.Cart, size = 48.dp)
            }
        }
    }
}
