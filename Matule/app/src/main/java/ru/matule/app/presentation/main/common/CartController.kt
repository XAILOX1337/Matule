package ru.matule.app.presentation.main.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.matule.app.domain.model.CartItem
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.AddToCartUseCase
import ru.matule.app.domain.usecase.ObserveCartUseCase
import ru.matule.app.domain.usecase.RefreshCartUseCase
import ru.matule.app.domain.usecase.RemoveFromCartUseCase
import ru.matule.app.presentation.common.Notifier

/**
 * Кнопки «Добавить» и «Убрать» на карточках описаний. Одна реализация для Главной, Каталога
 * и карточки товара: ViewModel экрана создаёт контроллер в своей области корутин.
 *
 * @param observeCart корзина как поток.
 * @param refreshCart загрузка корзины.
 * @param addToCart добавление.
 * @param removeFromCart удаление.
 * @param notifier уведомления об ошибках.
 * @param scope область корутин ViewModel.
 */
class CartController(
    observeCart: ObserveCartUseCase,
    private val refreshCart: RefreshCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val removeFromCart: RemoveFromCartUseCase,
    private val notifier: Notifier,
    private val scope: CoroutineScope,
) {
    /** Позиции корзины. */
    val items: StateFlow<List<CartItem>> = observeCart()

    private val pendingIds = MutableStateFlow<Set<String>>(emptySet())

    /** Товары, по которым идёт запрос: их кнопки временно неактивны. */
    val pending: StateFlow<Set<String>> = pendingIds.asStateFlow()

    /** Загрузить корзину, если она ещё не загружена. */
    fun load() {
        scope.launch {
            val result = refreshCart()
            if (result is AppResult.Failure) notifier.showError(result.error)
        }
    }

    /** «Добавить». */
    fun add(product: Product) = mutate(product.id) { addToCart(product) }

    /** «Убрать». */
    fun remove(productId: String) = mutate(productId) { removeFromCart(productId) }

    private fun mutate(productId: String, action: suspend () -> AppResult<Unit>) {
        if (productId in pendingIds.value) return
        pendingIds.update { it + productId }
        scope.launch {
            val result = action()
            pendingIds.update { it - productId }
            if (result is AppResult.Failure) notifier.showError(result.error)
        }
    }
}
