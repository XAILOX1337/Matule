package ru.matule.app.presentation.main.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.matule.app.di.matuleViewModel
import ru.matule.app.domain.model.CartItem
import ru.matule.app.presentation.common.Formatters
import ru.matule.app.presentation.main.common.EmptyListText
import ru.matule.uikit.components.button.MatuleButton
import ru.matule.uikit.components.card.MatuleCartCard
import ru.matule.uikit.components.header.MatuleHeader
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/**
 * Экран «Корзина».
 *
 * @param onBack «назад».
 * @param onOrderPlaced заказ оформлен — на Главную.
 */
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onOrderPlaced: () -> Unit,
) {
    val viewModel = matuleViewModel {
        CartViewModel(observeCart, refreshCart, changeCartCount, deleteCartItem, clearCart, placeOrder, notifier)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnOrderPlaced by rememberUpdatedState(onOrderPlaced)
    LaunchedEffect(viewModel) {
        viewModel.orderPlaced.collect { currentOnOrderPlaced() }
    }
    CartContent(
        state = state,
        onBack = onBack,
        onClear = viewModel::onClear,
        onIncrement = viewModel::onIncrement,
        onDecrement = viewModel::onDecrement,
        onRemove = viewModel::onRemove,
        onCheckout = viewModel::onCheckout,
    )
}

/**
 * Вёрстка экрана «Корзина».
 *
 * @param state состояние.
 * @param onBack «назад».
 * @param onClear очистить корзину.
 * @param onIncrement «+».
 * @param onDecrement «−».
 * @param onRemove удалить позицию.
 * @param onCheckout оформить заказ.
 */
@Composable
fun CartContent(
    state: CartUiState,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onIncrement: (CartItem) -> Unit,
    onDecrement: (CartItem) -> Unit,
    onRemove: (CartItem) -> Unit,
    onCheckout: () -> Unit,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.white)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        MatuleHeader(
            title = "Корзина",
            onBack = onBack,
            actionIcon = MatuleIcons.Trash,
            onAction = onClear,
            actionDescription = "Очистить корзину",
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp),
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                state.isLoading -> item(key = "loading") {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.accent)
                    }
                }
                state.items.isEmpty() -> item(key = "empty") {
                    EmptyListText(text = "В корзине пока ничего нет")
                }
                else -> {
                    items(state.items, key = { it.id }) { item ->
                        MatuleCartCard(
                            title = item.product.title,
                            price = Formatters.price(item.product.price),
                            countText = Formatters.pieces(item.count),
                            count = item.count,
                            onIncrement = { if (item.id !in state.pendingIds) onIncrement(item) },
                            onDecrement = { if (item.id !in state.pendingIds) onDecrement(item) },
                            onRemove = { if (item.id !in state.pendingIds) onRemove(item) },
                        )
                    }
                    item(key = "total") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Сумма",
                                style = typography.title2Semibold,
                                color = colors.black,
                                modifier = Modifier.weight(1f),
                            )
                            Text(text = Formatters.price(state.total), style = typography.title2Semibold, color = colors.black)
                        }
                    }
                }
            }
        }
        MatuleButton(
            text = "Перейти к оформлению заказа",
            onClick = onCheckout,
            enabled = state.items.isNotEmpty(),
            loading = state.isOrdering,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        )
    }
}
