package ru.matule.app.presentation.main.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.matule.app.domain.model.Category
import ru.matule.app.domain.model.Product
import ru.matule.app.presentation.common.Formatters
import ru.matule.uikit.components.card.MatuleProductCard
import ru.matule.uikit.components.chips.MatuleChipRow

/** Подпись чипа «все категории». */
private const val ALL_CATEGORIES = "Все"

/**
 * Ряд чипов категорий: «Все» и категории с сервера.
 *
 * @param categories категории.
 * @param selectedId выбранная категория; `null` — «Все».
 * @param onSelect выбор категории (`null` — «Все»).
 * @param modifier модификатор.
 * @param contentPadding отступы ряда.
 */
@Composable
fun CategoryChips(
    categories: List<Category>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val selectedIndex = categories.indexOfFirst { it.id == selectedId }.let { if (it < 0) 0 else it + 1 }
    MatuleChipRow(
        items = listOf(ALL_CATEGORIES) + categories.map { it.title },
        selectedIndex = selectedIndex,
        onSelect = { index -> onSelect(if (index == 0) null else categories[index - 1].id) },
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

/**
 * Карточка описания в списке (компонент UIKit с данными товара).
 *
 * @param product товар.
 * @param inCart товар в корзине — кнопка «Убрать».
 * @param onAdd «Добавить».
 * @param onRemove «Убрать».
 * @param onOpen открыть карточку товара.
 * @param modifier модификатор.
 */
@Composable
fun ProductItem(
    product: Product,
    inCart: Boolean,
    onAdd: (Product) -> Unit,
    onRemove: (Product) -> Unit,
    onOpen: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    MatuleProductCard(
        title = product.title,
        category = product.type,
        price = Formatters.price(product.price),
        inCart = inCart,
        onAddClick = { onAdd(product) },
        onRemoveClick = { onRemove(product) },
        onClick = { onOpen(product) },
        modifier = modifier,
    )
}
