package ru.matule.app.presentation.main.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.matule.app.di.matuleViewModel
import ru.matule.app.domain.model.Product
import ru.matule.app.presentation.common.Formatters
import ru.matule.app.presentation.main.common.CategoryChips
import ru.matule.app.presentation.main.common.EmptyListText
import ru.matule.app.presentation.main.common.MainTab
import ru.matule.app.presentation.main.common.MainTabScaffold
import ru.matule.app.presentation.main.common.ProductItem
import ru.matule.app.presentation.main.common.ProductSheet
import ru.matule.uikit.components.button.MatuleCartButton
import ru.matule.uikit.components.search.MatuleSearch
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/**
 * Экран «Каталог».
 *
 * @param onTabSelected переход на другую вкладку.
 * @param onOpenCart «В корзину».
 */
@Composable
fun CatalogScreen(
    onTabSelected: (MainTab) -> Unit,
    onOpenCart: () -> Unit,
) {
    val viewModel = matuleViewModel {
        CatalogViewModel(
            getCategories = getCategories,
            getCachedHome = getCachedHome,
            getCatalog = getCatalog,
            getCatalogFilter = getCatalogFilter,
            saveCatalogFilter = saveCatalogFilter,
            notifier = notifier,
            createCartController = { scope -> cartController(scope) },
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var openedProductId by rememberSaveable { mutableStateOf<String?>(null) }

    MainTabScaffold(selected = MainTab.Catalog, onTabSelected = onTabSelected) {
        CatalogContent(
            state = state,
            onQueryChange = viewModel::onQueryChange,
            onCategorySelected = viewModel::onCategorySelected,
            onAdd = viewModel.cart::add,
            onRemove = { product -> viewModel.cart.remove(product.id) },
            onOpen = { product -> openedProductId = product.id },
            onProfileClick = { onTabSelected(MainTab.Profile) },
            onOpenCart = onOpenCart,
        )
    }

    val opened = state.products.firstOrNull { it.id == openedProductId }
    if (opened != null) {
        ProductSheet(product = opened, onAdd = viewModel.cart::add, onDismiss = { openedProductId = null })
    }
}

/**
 * Вёрстка экрана «Каталог» (без нижнего меню).
 *
 * @param state состояние.
 * @param onQueryChange ввод поиска.
 * @param onCategorySelected выбор категории.
 * @param onAdd «Добавить».
 * @param onRemove «Убрать».
 * @param onOpen открыть карточку товара.
 * @param onProfileClick иконка профиля.
 * @param onOpenCart «В корзину».
 */
@Composable
fun CatalogContent(
    state: CatalogUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onAdd: (Product) -> Unit,
    onRemove: (Product) -> Unit,
    onOpen: (Product) -> Unit,
    onProfileClick: () -> Unit,
    onOpenCart: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val hasCart = state.cartTotal > 0
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MatuleSearch(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    onSearch = { focusManager.clearFocus() },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(24.dp))
                MatuleIcon(
                    icon = MatuleIcons.Profile,
                    size = 28.dp,
                    tint = MatuleTheme.colors.black,
                    contentDescription = "Профиль",
                    modifier = Modifier
                        .clip(MatuleTheme.shapes.circle)
                        .clickable(role = Role.Button, onClick = onProfileClick),
                )
            }
            CategoryChips(
                categories = state.categories,
                selectedId = state.selectedCategoryId,
                onSelect = onCategorySelected,
                contentPadding = PaddingValues(horizontal = 20.dp),
                modifier = Modifier.padding(top = 32.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = if (hasCart) CART_BUTTON_SPACE else 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when {
                    state.isLoading -> item(key = "loading") {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MatuleTheme.colors.accent)
                        }
                    }
                    state.products.isEmpty() -> item(key = "empty") {
                        EmptyListText(text = if (state.query.isBlank()) "Здесь пока пусто" else "Ничего не найдено")
                    }
                    else -> items(state.products, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            inCart = product.id in state.cartProductIds,
                            onAdd = { if (product.id !in state.pendingProductIds) onAdd(it) },
                            onRemove = { if (product.id !in state.pendingProductIds) onRemove(it) },
                            onOpen = onOpen,
                        )
                    }
                }
            }
        }
        if (hasCart) {
            MatuleCartButton(
                price = Formatters.price(state.cartTotal),
                onClick = onOpenCart,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            )
        }
    }
}

/** Место под плавающей кнопкой «В корзину», чтобы она не закрывала последнюю карточку. */
private val CART_BUTTON_SPACE = 104.dp
