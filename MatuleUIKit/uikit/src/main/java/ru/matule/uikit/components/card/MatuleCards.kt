package ru.matule.uikit.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.button.MatuleSmallButton
import ru.matule.uikit.components.button.MatuleSmallButtonStyle
import ru.matule.uikit.components.controls.MatuleCounter
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

private val CardShadowColor = Color(0x1A000000)

/**
 * Основа карточки (Card background): белый фон, скругление 12 dp, лёгкая тень и обводка.
 *
 * @param modifier модификатор карточки.
 * @param onClick обработчик нажатия на карточку; `null` — карточка не нажимается.
 * @param contentPadding внутренние отступы.
 * @param content содержимое карточки.
 */
@Composable
fun MatuleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MatuleTheme.shapes.large
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Column(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = shape, ambientColor = CardShadowColor, spotColor = CardShadowColor)
            .clip(shape)
            .background(MatuleTheme.colors.white)
            .border(1.dp, MatuleTheme.colors.cardStroke, shape)
            .then(clickModifier)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * Карточка описания товара (Карточки → Primary): название, категория, цена
 * и кнопка «Добавить» или «Убрать», если товар уже в корзине.
 *
 * @param title название товара.
 * @param category категория, например «Мужская одежда».
 * @param price цена, например «300 ₽».
 * @param inCart лежит ли товар в корзине: определяет кнопку.
 * @param onAddClick нажатие «Добавить».
 * @param onRemoveClick нажатие «Убрать».
 * @param modifier модификатор карточки.
 * @param onClick нажатие на карточку (открыть описание); `null` — не нажимается.
 * @param addText текст кнопки добавления.
 * @param removeText текст кнопки удаления.
 */
@Composable
fun MatuleProductCard(
    title: String,
    category: String,
    price: String,
    inCart: Boolean,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    addText: String = "Добавить",
    removeText: String = "Убрать",
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    MatuleCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Text(
            text = title,
            style = typography.headlineMedium,
            color = colors.black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = category, style = typography.captionSemibold, color = colors.placeholder, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(text = price, style = typography.title3Semibold, color = colors.black, maxLines = 1)
            }
            Spacer(Modifier.width(12.dp))
            if (inCart) {
                MatuleSmallButton(text = removeText, onClick = onRemoveClick, style = MatuleSmallButtonStyle.Outlined)
            } else {
                MatuleSmallButton(text = addText, onClick = onAddClick)
            }
        }
    }
}

/**
 * Карточка позиции в корзине (Карточки → Cart): название, кнопка удаления, цена,
 * количество и счётчик.
 *
 * @param title название товара.
 * @param price цена позиции, например «300 ₽».
 * @param countText подпись количества, например «1 штука».
 * @param count текущее количество.
 * @param onIncrement нажатие «+».
 * @param onDecrement нажатие «−».
 * @param onRemove нажатие «×» — удалить позицию.
 * @param modifier модификатор карточки.
 * @param minCount минимальное количество: при нём «−» неактивен.
 */
@Composable
fun MatuleCartCard(
    title: String,
    price: String,
    countText: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    minCount: Int = 1,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    MatuleCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = title,
                style = typography.headlineMedium,
                color = colors.black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            MatuleIcon(
                icon = MatuleIcons.Close,
                size = 20.dp,
                tint = colors.inputIcon,
                contentDescription = "Удалить из корзины",
                modifier = Modifier
                    .clip(MatuleTheme.shapes.circle)
                    .clickable(onClick = onRemove),
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = price, style = typography.title3Medium, color = colors.black, modifier = Modifier.weight(1f))
            Text(text = countText, style = typography.captionRegular, color = colors.black)
            Spacer(Modifier.width(16.dp))
            MatuleCounter(count = count, onIncrement = onIncrement, onDecrement = onDecrement, minCount = minCount)
        }
    }
}

/**
 * Карточка проекта (Карточки → Project): название, сколько прошло времени и кнопка «Открыть».
 *
 * @param title название проекта.
 * @param subtitle вторичный текст, например «Прошло 2 дня».
 * @param onAction нажатие кнопки справа.
 * @param modifier модификатор карточки.
 * @param actionText текст кнопки.
 * @param onClick нажатие на карточку; `null` — не нажимается.
 */
@Composable
fun MatuleProjectCard(
    title: String,
    subtitle: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String = "Открыть",
    onClick: (() -> Unit)? = null,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    MatuleCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Text(
            text = title,
            style = typography.headlineMedium,
            color = colors.black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = subtitle,
                style = typography.captionSemibold,
                color = colors.placeholder,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            MatuleSmallButton(text = actionText, onClick = onAction)
        }
    }
}

/**
 * Баннер акции («Акции и новости» на главной): цветной фон, название, цена и картинка справа.
 *
 * @param title название акции.
 * @param price цена, например «4000 ₽».
 * @param background фон баннера (цвет или градиент).
 * @param modifier модификатор баннера.
 * @param width ширина баннера.
 * @param height высота баннера.
 * @param onClick нажатие на баннер; `null` — не нажимается.
 * @param image картинка в правой части; `null` — без картинки.
 */
@Composable
fun MatulePromoCard(
    title: String,
    price: String,
    background: Brush,
    modifier: Modifier = Modifier,
    width: Dp = 270.dp,
    height: Dp = 152.dp,
    onClick: (() -> Unit)? = null,
    image: (@Composable BoxScope.() -> Unit)? = null,
) {
    val shape = MatuleTheme.shapes.large
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(background)
            .then(clickModifier),
    ) {
        if (image != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(width * 0.45f),
                contentAlignment = Alignment.Center,
                content = image,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(width * 0.6f)
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MatuleTheme.typography.title2ExtraBold,
                color = MatuleTheme.colors.white,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = price, style = MatuleTheme.typography.title2ExtraBold, color = MatuleTheme.colors.white, maxLines = 1)
        }
    }
}
