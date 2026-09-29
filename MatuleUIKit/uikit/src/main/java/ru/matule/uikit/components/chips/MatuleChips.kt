package ru.matule.uikit.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.matule.uikit.theme.MatuleSizes
import ru.matule.uikit.theme.MatuleTheme

/**
 * Чип категории (Кнопки → Chips): активный — синий с белым текстом, неактивный — серый.
 *
 * @param text название категории.
 * @param selected выбран ли чип.
 * @param onClick обработчик нажатия.
 * @param modifier модификатор чипа.
 */
@Composable
fun MatuleChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MatuleTheme.colors
    Box(
        modifier = modifier
            .heightIn(min = MatuleSizes.chipHeight)
            .clip(MatuleTheme.shapes.medium)
            .background(if (selected) colors.accent else colors.inputBackground)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MatuleTheme.typography.textMedium,
            color = if (selected) colors.white else colors.description,
            maxLines = 1,
        )
    }
}

/**
 * Горизонтальный ряд чипов с прокруткой (Категории меню).
 *
 * @param items названия чипов.
 * @param selectedIndex индекс выбранного чипа; -1 — ничего не выбрано.
 * @param onSelect вызывается с индексом нажатого чипа.
 * @param modifier модификатор ряда.
 * @param contentPadding отступы ряда от краёв экрана.
 */
@Composable
fun MatuleChipRow(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(items) { index, title ->
            MatuleChip(text = title, selected = index == selectedIndex, onClick = { onSelect(index) })
        }
    }
}
