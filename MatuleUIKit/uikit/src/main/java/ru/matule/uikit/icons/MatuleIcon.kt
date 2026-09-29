package ru.matule.uikit.icons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.matule.uikit.R
import ru.matule.uikit.theme.MatuleTheme

/** Шрифт, собранный из иконок макета (`res/font/matule_icons.ttf`). */
val MatuleIconFont = FontFamily(Font(R.font.matule_icons))

/**
 * Иконка Matule: рисует глиф иконочного шрифта.
 * Размер задаётся в dp и не зависит от системного масштаба шрифта.
 *
 * @param icon какую иконку показать.
 * @param modifier модификатор контейнера иконки.
 * @param size сторона квадрата иконки.
 * @param tint цвет иконки.
 * @param contentDescription описание для TalkBack; `null` — иконка декоративная.
 */
@Composable
fun MatuleIcon(
    icon: MatuleIcons,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MatuleTheme.colors.black,
    contentDescription: String? = null,
) {
    val fontSize = with(LocalDensity.current) { size.toSp() }
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .size(size)
            .then(semanticsModifier),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = icon.glyph,
            style = TextStyle(
                color = tint,
                fontFamily = MatuleIconFont,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            softWrap = false,
        )
    }
}
