package ru.matule.uikit.components.misc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.matule.uikit.R
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/**
 * Логотип Matule (Логотип).
 *
 * @param modifier модификатор изображения.
 * @param size сторона квадрата логотипа.
 */
@Composable
fun MatuleLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    Image(
        painter = painterResource(R.drawable.ic_matule_logo),
        contentDescription = "Логотип Matule",
        modifier = modifier.size(size),
    )
}

/**
 * Пункт меню профиля: смайлик, название и необязательный элемент справа (например, переключатель).
 *
 * @param title название пункта.
 * @param modifier модификатор строки.
 * @param emoji смайлик слева; `null` — без смайлика.
 * @param onClick нажатие на пункт; `null` — пункт не нажимается.
 * @param trailing содержимое справа; `null` — пусто.
 */
@Composable
fun MatuleMenuItem(
    title: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(MatuleTheme.shapes.medium)
            .then(clickModifier)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (emoji != null) {
            Text(text = emoji, style = MatuleTheme.typography.title2Regular)
            Spacer(Modifier.width(16.dp))
        }
        Text(
            text = title,
            style = MatuleTheme.typography.title3Semibold,
            color = MatuleTheme.colors.black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

/**
 * Плитка добавления фото («+» на экране «Создать проект»).
 * Пока фото нет — серая плитка с плюсом, после выбора — превью из [image].
 *
 * @param onClick нажатие на плитку (открыть выбор источника фото).
 * @param modifier модификатор плитки.
 * @param size сторона плитки.
 * @param error подсветить ли плитку красным (фото обязательно).
 * @param image превью выбранного фото; `null` — показывается плюс.
 */
@Composable
fun MatuleImagePicker(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    error: Boolean = false,
    image: (@Composable () -> Unit)? = null,
) {
    val colors = MatuleTheme.colors
    val shape = MatuleTheme.shapes.large
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(colors.inputBackground)
            .border(1.dp, if (error) colors.error else colors.inputStroke, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            image()
        } else {
            MatuleIcon(icon = MatuleIcons.Plus, size = 40.dp, tint = colors.inputIcon, contentDescription = "Добавить фото")
        }
    }
}
