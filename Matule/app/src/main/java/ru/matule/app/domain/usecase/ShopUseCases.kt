package ru.matule.app.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import ru.matule.app.domain.model.CartItem
import ru.matule.app.domain.model.Category
import ru.matule.app.domain.model.HomeData
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.repository.AppStateRepository
import ru.matule.app.domain.repository.CartRepository
import ru.matule.app.domain.repository.CatalogRepository
import ru.matule.app.domain.repository.OrderRepository
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult

/** Главная из кэша — показывается сразу и без сети. */
class GetCachedHomeUseCase(private val catalog: CatalogRepository) {
    suspend operator fun invoke(): HomeData? = catalog.cachedHome()
}

/** Главная с сервера: акции, категории, описания. */
class LoadHomeUseCase(private val catalog: CatalogRepository) {
    suspend operator fun invoke(): AppResult<HomeData> = catalog.loadHome()
}

/** Категории каталога. */
class GetCategoriesUseCase(private val catalog: CatalogRepository) {
    suspend operator fun invoke(): AppResult<List<Category>> = catalog.categories()
}

/** Каталог: поиск, если есть запрос, иначе список категории. */
class GetCatalogUseCase(private val catalog: CatalogRepository) {
    suspend operator fun invoke(query: String, categoryId: String?): AppResult<List<Product>> {
        val text = query.trim()
        return if (text.isEmpty()) catalog.products(categoryId) else catalog.search(text, categoryId)
    }
}

/** Полное описание товара. */
class GetProductUseCase(private val catalog: CatalogRepository) {
    suspend operator fun invoke(productId: String): AppResult<Product> = catalog.product(productId)
}

/** Сохранённые поиск и категория каталога. */
class GetCatalogFilterUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke(): Pair<String, String?> = appState.catalogFilter()
}

/** Сохранение поиска и категории каталога. */
class SaveCatalogFilterUseCase(private val appState: AppStateRepository) {
    suspend operator fun invoke(query: String, categoryId: String?) = appState.saveCatalogFilter(query, categoryId)
}

/** Корзина как поток: экраны перерисовываются при любом изменении. */
class ObserveCartUseCase(private val cart: CartRepository) {
    operator fun invoke(): StateFlow<List<CartItem>> = cart.items
}

/** Загрузка корзины с сервера. */
class RefreshCartUseCase(private val cart: CartRepository) {
    /**
     * @param force загрузить заново, даже если корзина уже загружена.
     */
    suspend operator fun invoke(force: Boolean = false): AppResult<List<CartItem>> =
        if (!force && cart.isLoaded) AppResult.Success(cart.items.value) else cart.refresh()
}

/** Добавление в корзину. */
class AddToCartUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(product: Product): AppResult<Unit> = cart.add(product)
}

/** Удаление товара из корзины (кнопка «Убрать»). */
class RemoveFromCartUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(productId: String): AppResult<Unit> = cart.removeProduct(productId)
}

/** Изменение количества. */
class ChangeCartCountUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(itemId: String, count: Int): AppResult<Unit> {
        if (count !in MIN_COUNT..MAX_COUNT) {
            return AppResult.Failure(AppError.Validation("Количество — от $MIN_COUNT до $MAX_COUNT"))
        }
        return cart.changeCount(itemId, count)
    }

    companion object {
        /** Минимум в позиции. */
        const val MIN_COUNT = 1

        /** Максимум в позиции. */
        const val MAX_COUNT = 99
    }
}

/** Удаление позиции (крестик в карточке корзины). */
class DeleteCartItemUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(itemId: String): AppResult<Unit> = cart.removeItem(itemId)
}

/** Очистка корзины (корзина в хэдере). */
class ClearCartUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(): AppResult<Unit> = cart.clear()
}

/** Оформление заказа: заказ на сервере, затем корзина очищается. */
class PlaceOrderUseCase(
    private val cart: CartRepository,
    private val orders: OrderRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val items = cart.items.value
        if (items.isEmpty()) return AppResult.Failure(AppError.Validation("Корзина пуста"))
        return when (val result = orders.placeOrder(items)) {
            is AppResult.Failure -> result
            // Заказ уже создан; если очистить корзину не вышло, она очистится при следующей попытке.
            is AppResult.Success -> {
                cart.clear()
                AppResult.Success(Unit)
            }
        }
    }
}
