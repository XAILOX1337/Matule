package ru.matule.uikit.components.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.button.MatuleBubbleButton
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/** Вид хэдера из макета (Хэдер). */
enum class MatuleHeaderStyle {
    /** Кнопки сверху, крупный заголовок ниже слева (экран «Корзина»). */
    Large,

    /** Заголовок по центру, кнопки по краям в одной строке («Проекты», «Создать проект»). */
    Compact,
}

/**
 * Хэдер экрана: заголовок, кнопка «назад» слева и кнопка действия справа.
 *
 * @param title заголовок экрана.
 * @param modifier модификатор хэдера.
 * @param style вид: [MatuleHeaderStyle.Large] или [MatuleHeaderStyle.Compact].
 * @param onBack нажатие «назад»; `null` — кнопки «назад» нет.
 * @param actionIcon иконка действия справа (например, удалить или «+»); `null` — нет.
 * @param onAction нажатие на действие справа.
 * @param actionDescription описание действия для TalkBack.
 */
@Composable
fun MatuleHeader(
    title: String,
    modifier: Modifier = Modifier,
    style: MatuleHeaderStyle = MatuleHeaderStyle.Large,
    onBack: (() -> Unit)? = null,
    actionIcon: MatuleIcons? = null,
    onAction: (() -> Unit)? = null,
    actionDescription: String? = actionIcon?.title,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    val back: @Composable () -> Unit = {
        if (onBack != null) {
            MatuleBubbleButton(icon = MatuleIcons.Back, onClick = onBack, tint = colors.black)
        } else {
            Spacer(Modifier.size(32.dp))
        }
    }
    val action: @Composable () -> Unit = {
        if (actionIcon != null) {
            MatuleIcon(
                icon = actionIcon,
                size = 24.dp,
                tint = if (actionIcon == MatuleIcons.Trash) colors.inputIcon else colors.black,
                contentDescription = actionDescription,
                modifier = Modifier
                    .clip(MatuleTheme.shapes.circle)
                    .clickable(enabled = onAction != null) { onAction?.invoke() },
            )
        } else {
            Spacer(Modifier.size(24.dp))
        }
    }

    when (style) {
        MatuleHeaderStyle.Large -> Column(modifier = modifier.fillMaxWidth()) {
            if (onBack != null || actionIcon != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                ) {
                    back()
                    action()
                }
                Spacer(Modifier.height(16.dp))
            }
            Text(
                text = title,
                style = typography.title1ExtraBold,
                color = colors.black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        MatuleHeaderStyle.Compact -> Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.align(Alignment.CenterStart)) { back() }
            Text(
                text = title,
                style = typography.title2Semibold,
                color = colors.black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 44.dp),
            )
            Box(modifier = Modifier.align(Alignment.CenterEnd)) { action() }
        }
    }
}
