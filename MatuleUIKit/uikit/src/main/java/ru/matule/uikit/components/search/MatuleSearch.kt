package ru.matule.uikit.components.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.button.MatuleBubbleButton
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleSizes
import ru.matule.uikit.theme.MatuleTheme

/**
 * Поле поиска (Поиск → Search/Big): лупа слева, кнопка очистки «×» справа, когда есть текст.
 *
 * @param query текущий запрос.
 * @param onQueryChange вызывается при вводе текста и при очистке (с пустой строкой).
 * @param modifier модификатор поля.
 * @param placeholder подсказка в пустом поле.
 * @param onSearch вызывается по кнопке «Поиск» на клавиатуре; `null` — ничего не делать.
 * @param focusRequester позволяет программно поставить фокус в поле.
 */
@Composable
fun MatuleSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Искать описания",
    onSearch: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    val fieldModifier = if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .then(fieldModifier),
        textStyle = typography.textRegular.copy(color = colors.black),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        cursorBrush = SolidColor(colors.accent),
        decorationBox = { innerTextField ->
            SearchContainer {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = typography.textRegular,
                            color = colors.placeholder,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    MatuleBubbleButton(
                        icon = MatuleIcons.Close,
                        onClick = { onQueryChange("") },
                        size = 24.dp,
                        iconSize = 16.dp,
                        contentDescription = "Очистить поиск",
                    )
                }
            }
        },
    )
}

/**
 * Поле поиска, которое не принимает ввод, а открывает поиск по нажатию (например, на главной).
 *
 * @param onClick обработчик нажатия.
 * @param modifier модификатор поля.
 * @param placeholder подсказка в поле.
 */
@Composable
fun MatuleSearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Искать описания",
) {
    SearchContainer(
        modifier = modifier
            .clip(MatuleTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(
            text = placeholder,
            style = MatuleTheme.typography.textRegular,
            color = MatuleTheme.colors.placeholder,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Общая рамка поля поиска с лупой (DRY для редактируемого и нажимаемого вариантов). */
@Composable
private fun SearchContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = MatuleTheme.colors
    val shape = MatuleTheme.shapes.medium
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MatuleSizes.inputHeight)
            .clip(shape)
            .background(colors.inputBackground)
            .border(1.dp, colors.inputStroke, shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MatuleIcon(icon = MatuleIcons.Search, size = 20.dp, tint = colors.inputIcon)
        Spacer(Modifier.width(8.dp))
        content()
    }
}
