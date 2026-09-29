package ru.matule.uikit.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleSizes
import ru.matule.uikit.theme.MatuleTheme

/** Вид большой кнопки из макета (Кнопки → Big). */
enum class MatuleButtonStyle {
    /** Синяя кнопка основного действия. В неактивном состоянии — бледно-синяя. */
    Primary,

    /** Белая кнопка с синей обводкой и синим текстом. */
    Secondary,

    /** Серая кнопка с чёрным текстом. */
    Tertiary,
}

/** Вид маленькой кнопки из макета (Кнопки → Small). */
enum class MatuleSmallButtonStyle {
    /** Синяя кнопка, например «Добавить». */
    Primary,

    /** Белая с синей обводкой, например «Убрать». */
    Outlined,

    /** Серая кнопка. */
    Tertiary,
}

/**
 * Большая кнопка на всю ширину (высота 56 dp).
 *
 * @param text надпись на кнопке.
 * @param onClick обработчик нажатия.
 * @param modifier модификатор кнопки.
 * @param style вид кнопки: [MatuleButtonStyle.Primary], [MatuleButtonStyle.Secondary] или [MatuleButtonStyle.Tertiary].
 * @param enabled активна ли кнопка; неактивная не реагирует на нажатия и меняет цвет.
 * @param loading показывать ли индикатор загрузки вместо текста (кнопка при этом не нажимается).
 */
@Composable
fun MatuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MatuleButtonStyle = MatuleButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = MatuleTheme.colors
    val palette = when (style) {
        MatuleButtonStyle.Primary -> ButtonPalette(
            container = if (enabled) colors.accent else colors.accentInactive,
            content = colors.white,
        )
        MatuleButtonStyle.Secondary -> ButtonPalette(
            container = colors.white,
            content = if (enabled) colors.accent else colors.accentInactive,
            border = if (enabled) colors.accent else colors.accentInactive,
        )
        MatuleButtonStyle.Tertiary -> ButtonPalette(
            container = colors.inputBackground,
            content = if (enabled) colors.black else colors.placeholder,
        )
    }
    MatuleButtonBase(
        text = text,
        onClick = onClick,
        palette = palette,
        textStyle = MatuleTheme.typography.title3Semibold,
        minHeight = MatuleSizes.bigButtonHeight,
        shape = MatuleTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        enabled = enabled,
        loading = loading,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Маленькая кнопка (высота 32 dp) — «Добавить», «Убрать», «Открыть» в карточках.
 *
 * @param text надпись на кнопке.
 * @param onClick обработчик нажатия.
 * @param modifier модификатор кнопки.
 * @param style вид кнопки: [MatuleSmallButtonStyle.Primary], [MatuleSmallButtonStyle.Outlined] или [MatuleSmallButtonStyle.Tertiary].
 * @param enabled активна ли кнопка.
 */
@Composable
fun MatuleSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MatuleSmallButtonStyle = MatuleSmallButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val colors = MatuleTheme.colors
    val palette = when (style) {
        MatuleSmallButtonStyle.Primary -> ButtonPalette(
            container = if (enabled) colors.accent else colors.accentInactive,
            content = colors.white,
        )
        MatuleSmallButtonStyle.Outlined -> ButtonPalette(
            container = colors.white,
            content = if (enabled) colors.accent else colors.accentInactive,
            border = if (enabled) colors.accent else colors.accentInactive,
        )
        MatuleSmallButtonStyle.Tertiary -> ButtonPalette(
            container = colors.inputBackground,
            content = if (enabled) colors.black else colors.placeholder,
        )
    }
    MatuleButtonBase(
        text = text,
        onClick = onClick,
        palette = palette,
        textStyle = MatuleTheme.typography.captionSemibold,
        minHeight = MatuleSizes.smallButtonHeight,
        shape = MatuleTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        enabled = enabled,
        loading = false,
        modifier = modifier,
    )
}

/**
 * Кнопка «В корзину» с иконкой тележки и суммой справа (Кнопки → Cart).
 *
 * @param price сумма, например «500 ₽».
 * @param onClick обработчик нажатия.
 * @param modifier модификатор кнопки.
 * @param text надпись слева.
 */
@Composable
fun MatuleCartButton(
    price: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "В корзину",
) {
    val colors = MatuleTheme.colors
    val shape = MatuleTheme.shapes.medium
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MatuleSizes.bigButtonHeight)
            .clip(shape)
            .background(colors.accent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MatuleIcon(icon = MatuleIcons.Cart, tint = colors.white)
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            style = MatuleTheme.typography.title3Semibold,
            color = colors.white,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(text = price, style = MatuleTheme.typography.title3Semibold, color = colors.white, maxLines = 1)
    }
}

/**
 * Кнопка входа через сторонний сервис (Кнопки → Log in): белая, с обводкой, иконка слева от текста.
 *
 * @param text надпись, например «Войти с VK».
 * @param onClick обработчик нажатия.
 * @param modifier модификатор кнопки.
 * @param icon иконка сервиса; `null` — без иконки.
 */
@Composable
fun MatuleLoginButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = MatuleTheme.colors
    val shape = MatuleTheme.shapes.medium
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MatuleSizes.bigButtonHeight)
            .clip(shape)
            .background(colors.white)
            .border(BorderStroke(1.dp, colors.inputStroke), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        if (icon != null) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
            Spacer(Modifier.width(12.dp))
        }
        Text(text = text, style = MatuleTheme.typography.headlineMedium, color = colors.black, maxLines = 1)
    }
}

/**
 * Круглая кнопка-иконка (Кнопки → Bubble): «назад» в хэдере, «закрыть» в модальном окне и уведомлении.
 *
 * @param icon иконка внутри кнопки.
 * @param onClick обработчик нажатия.
 * @param modifier модификатор кнопки.
 * @param size диаметр кнопки.
 * @param iconSize размер иконки.
 * @param background цвет круга.
 * @param tint цвет иконки.
 * @param contentDescription описание для TalkBack; по умолчанию — название иконки.
 */
@Composable
fun MatuleBubbleButton(
    icon: MatuleIcons,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = MatuleSizes.bubble,
    iconSize: Dp = 20.dp,
    background: Color = MatuleTheme.colors.inputBackground,
    tint: Color = MatuleTheme.colors.inputIcon,
    contentDescription: String? = icon.title,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(MatuleTheme.shapes.circle)
            .background(background)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        MatuleIcon(icon = icon, size = iconSize, tint = tint, contentDescription = contentDescription)
    }
}

/**
 * Текстовая кнопка-ссылка, например «Зарегистрироваться».
 *
 * @param text текст ссылки.
 * @param onClick обработчик нажатия.
 * @param modifier модификатор.
 * @param color цвет текста.
 * @param style стиль текста.
 */
@Composable
fun MatuleTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MatuleTheme.colors.accent,
    style: TextStyle = MatuleTheme.typography.textRegular,
) {
    Text(
        text = text,
        style = style,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(MatuleTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    )
}

/** Цвета конкретного вида кнопки. */
internal data class ButtonPalette(val container: Color, val content: Color, val border: Color? = null)

/**
 * Общая основа всех текстовых кнопок (DRY): форма, фон, обводка, нажатие, текст или индикатор загрузки.
 *
 * @param text надпись.
 * @param onClick обработчик нажатия.
 * @param palette цвета фона, текста и обводки.
 * @param textStyle стиль надписи.
 * @param minHeight минимальная высота.
 * @param shape форма кнопки.
 * @param contentPadding внутренние отступы.
 * @param enabled активна ли кнопка.
 * @param loading показывать ли индикатор загрузки.
 * @param modifier модификатор.
 */
@Composable
internal fun MatuleButtonBase(
    text: String,
    onClick: () -> Unit,
    palette: ButtonPalette,
    textStyle: TextStyle,
    minHeight: Dp,
    shape: Shape,
    contentPadding: PaddingValues,
    enabled: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderModifier = if (palette.border != null) Modifier.border(1.dp, palette.border, shape) else Modifier
    Box(
        modifier = modifier
            .heightIn(min = minHeight)
            .clip(shape)
            .background(palette.container)
            .then(borderModifier)
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = palette.content,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp),
            )
        } else {
            Text(
                text = text,
                style = textStyle,
                color = palette.content,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
