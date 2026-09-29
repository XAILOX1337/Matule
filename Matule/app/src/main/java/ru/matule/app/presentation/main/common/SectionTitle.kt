package ru.matule.app.presentation.main.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.matule.uikit.theme.MatuleTheme

/**
 * Заголовок раздела на экране («Акции и новости», «Каталог описаний»).
 *
 * @param text текст.
 * @param modifier модификатор.
 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MatuleTheme.typography.headlineRegular,
        color = MatuleTheme.colors.description,
        modifier = modifier,
    )
}

/**
 * Сообщение вместо пустого списка.
 *
 * @param text текст.
 * @param modifier модификатор.
 */
@Composable
fun EmptyListText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MatuleTheme.typography.textRegular,
        color = MatuleTheme.colors.placeholder,
        modifier = modifier,
    )
}
