package ru.matule.app.presentation.main.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.matule.app.domain.model.CartItem
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.ChangeCartCountUseCase
import ru.matule.app.domain.usecase.ClearCartUseCase
import ru.matule.app.domain.usecase.DeleteCartItemUseCase
import ru.matule.app.domain.usecase.ObserveCartUseCase
import ru.matule.app.domain.usecase.PlaceOrderUseCase
import ru.matule.app.domain.usecase.RefreshCartUseCase
import ru.matule.app.presentation.common.Notifier

/**
 * Состояние экрана «Корзина».
 *
 * @property items позиции.
 * @property total сумма.
 * @property pendingIds позиции, по которым идёт запрос.
 * @property isLoading загрузка корзины.
 * @property isOrdering оформление заказа.
 */
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val total: Int = 0,
    val pendingIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isOrdering: Boolean = false,
)

/** Служебное состояние экрана. */
private data class CartFlags(
    val pendingIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isOrdering: Boolean = false,
)

/**
 * Экран «Корзина»: изменение количества, удаление, очистка, оформление заказа.
 *
 * @param observeCart корзина как поток.
 * @param refreshCart загрузка корзины.
 * @param changeCount изменение количества.
 * @param deleteItem удаление позиции.
 * @param clearCart очистка.
 * @param placeOrder оформление заказа.
 * @param notifier уведомления.
 */
class CartViewModel(
    observeCart: ObserveCartUseCase,
    private val refreshCart: RefreshCartUseCase,
    private val changeCount: ChangeCartCountUseCase,
    private val deleteItem: DeleteCartItemUseCase,
    private val clearCart: ClearCartUseCase,
    private val placeOrder: PlaceOrderUseCase,
    private val notifier: Notifier,
) : ViewModel() {

    private val flags = MutableStateFlow(CartFlags())

    /** Состояние экрана. */
    val state: StateFlow<CartUiState> = combine(observeCart(), flags) { items, current ->
        CartUiState(
            items = items,
            total = items.sumOf { it.total },
            pendingIds = current.pendingIds,
            isLoading = current.isLoading && items.isEmpty(),
            isOrdering = current.isOrdering,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CartUiState(isLoading = true))

    private val orderEvents = Channel<Unit>(Channel.BUFFERED)

    /** Заказ оформлен и уведомление показано — пора на Главную. */
    val orderPlaced: Flow<Unit> = orderEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            val result = refreshCart(force = true)
            flags.update { it.copy(isLoading = false) }
            if (result is AppResult.Failure) notifier.showError(result.error)
        }
    }

    /** «+». */
    fun onIncrement(item: CartItem) = mutate(item.id) { changeCount(item.id, item.count + 1) }

    /** «−». */
    fun onDecrement(item: CartItem) {
        if (item.count <= ChangeCartCountUseCase.MIN_COUNT) return
        mutate(item.id) { changeCount(item.id, item.count - 1) }
    }

    /** Крестик в карточке. */
    fun onRemove(item: CartItem) = mutate(item.id) { deleteItem(item.id) }

    /** Очистить корзину. */
    fun onClear() {
        if (state.value.items.isEmpty()) return
        mutate(CLEAR_KEY) { clearCart() }
    }

    /** «Перейти к оформлению заказа». */
    fun onCheckout() {
        if (flags.value.isOrdering || state.value.items.isEmpty()) return
        flags.update { it.copy(isOrdering = true) }
        viewModelScope.launch {
            when (val result = placeOrder()) {
                is AppResult.Success -> {
                    notifier.showSuccess("Заказ оформлен", "Спасибо за покупку! Описания уже ждут вас")
                    delay(SUCCESS_DELAY_MS)
                    orderEvents.send(Unit)
                }
                is AppResult.Failure -> notifier.showError(result.error)
            }
            flags.update { it.copy(isOrdering = false) }
        }
    }

    private fun mutate(key: String, action: suspend () -> AppResult<Unit>) {
        if (key in flags.value.pendingIds) return
        flags.update { it.copy(pendingIds = it.pendingIds + key) }
        viewModelScope.launch {
            val result = action()
            flags.update { it.copy(pendingIds = it.pendingIds - key) }
            if (result is AppResult.Failure) notifier.showError(result.error)
        }
    }

    private companion object {
        const val CLEAR_KEY = "clear"

        /** Уведомление об успешном заказе видно 5 секунд, затем — Главная. */
        const val SUCCESS_DELAY_MS = 5_000L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
