package ru.matule.app.presentation.main.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ru.matule.app.di.matuleViewModel
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.model.Promotion
import ru.matule.app.presentation.common.Formatters
import ru.matule.app.presentation.common.NotificationPermissionRequest
import ru.matule.app.presentation.common.parseHexColor
import ru.matule.app.presentation.main.common.CategoryChips
import ru.matule.app.presentation.main.common.EmptyListText
import ru.matule.app.presentation.main.common.MainTab
import ru.matule.app.presentation.main.common.MainTabScaffold
import ru.matule.app.presentation.main.common.ProductItem
import ru.matule.app.presentation.main.common.ProductSheet
import ru.matule.app.presentation.main.common.SectionTitle
import ru.matule.uikit.components.card.MatulePromoCard
import ru.matule.uikit.components.search.MatuleSearch
import ru.matule.uikit.theme.MatuleTheme

/**
 * Экран «Главная».
 *
 * @param onTabSelected переход на другую вкладку.
 */
@Composable
fun HomeScreen(onTabSelected: (MainTab) -> Unit) {
    val viewModel = matuleViewModel {
        HomeViewModel(
            getCachedHome = getCachedHome,
            loadHome = loadHome,
            isNotificationsEnabled = isNotificationsEnabled,
            notifier = notifier,
            createCartController = { scope -> cartController(scope) },
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    NotificationPermissionRequest(enabled = notificationsEnabled)
    var openedProductId by rememberSaveable { mutableStateOf<String?>(null) }

    MainTabScaffold(selected = MainTab.Home, onTabSelected = onTabSelected) {
        HomeContent(
            state = state,
            onQueryChange = viewModel::onQueryChange,
            onCategorySelected = viewModel::onCategorySelected,
            onAdd = viewModel.cart::add,
            onRemove = { product -> viewModel.cart.remove(product.id) },
            onOpen = { product -> openedProductId = product.id },
        )
    }

    val opened = state.products.firstOrNull { it.id == openedProductId }
    if (opened != null) {
        ProductSheet(product = opened, onAdd = viewModel.cart::add, onDismiss = { openedProductId = null })
    }
}

/**
 * Вёрстка экрана «Главная» (без нижнего меню).
 *
 * @param state состояние.
 * @param onQueryChange ввод поиска.
 * @param onCategorySelected выбор категории.
 * @param onAdd «Добавить».
 * @param onRemove «Убрать».
 * @param onOpen открыть карточку товара.
 */
@Composable
fun HomeContent(
    state: HomeUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onAdd: (Product) -> Unit,
    onRemove: (Product) -> Unit,
    onOpen: (Product) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val horizontal = Modifier.padding(horizontal = 20.dp)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "search") {
            MatuleSearch(
                query = state.query,
                onQueryChange = onQueryChange,
                onSearch = { focusManager.clearFocus() },
                modifier = horizontal.padding(top = 24.dp),
            )
        }
        if (state.promotions.isNotEmpty()) {
            item(key = "promotions_title") {
                SectionTitle(text = "Акции и новости", modifier = horizontal.padding(top = 32.dp))
            }
            item(key = "promotions") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    items(state.promotions, key = { it.id }) { promotion -> PromotionCard(promotion) }
                }
            }
        }
        item(key = "catalog_title") {
            SectionTitle(text = "Каталог описаний", modifier = horizontal.padding(top = 32.dp))
        }
        item(key = "categories") {
            CategoryChips(
                categories = state.categories,
                selectedId = state.selectedCategoryId,
                onSelect = onCategorySelected,
                contentPadding = PaddingValues(horizontal = 20.dp),
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
        }
        when {
            state.isLoading -> item(key = "loading") {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MatuleTheme.colors.accent)
                }
            }
            state.products.isEmpty() -> item(key = "empty") {
                EmptyListText(text = "Ничего не найдено", modifier = horizontal.padding(top = 16.dp))
            }
            else -> items(state.products, key = { it.id }) { product ->
                ProductItem(
                    product = product,
                    inCart = product.id in state.cartProductIds,
                    onAdd = { if (product.id !in state.pendingProductIds) onAdd(it) },
                    onRemove = { if (product.id !in state.pendingProductIds) onRemove(it) },
                    onOpen = onOpen,
                    modifier = horizontal.padding(top = 16.dp),
                )
            }
        }
    }
}

/**
 * Баннер акции: цвет с сервера, название, цена и картинка (если есть).
 *
 * @param promotion акция.
 */
@Composable
private fun PromotionCard(promotion: Promotion) {
    val background = parseHexColor(promotion.colorHex, MatuleTheme.colors.accent)
    val imageUrl = promotion.imageUrl
    MatulePromoCard(
        title = promotion.title,
        price = Formatters.price(promotion.price),
        background = SolidColor(background),
        image = {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = promotion.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                )
            }
        },
    )
}
