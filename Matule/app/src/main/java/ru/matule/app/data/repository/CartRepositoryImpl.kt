package ru.matule.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.matule.app.data.local.SessionLocalDataSource
import ru.matule.app.data.remote.CartRemoteDataSource
import ru.matule.app.domain.model.CartItem
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.repository.CartRepository
import ru.matule.app.domain.repository.UserDataCleaner
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.result.map

/**
 * Корзина: сервер — источник правды, в памяти — общий для экранов поток позиций.
 * Операции выполняются по очереди, чтобы быстрые нажатия не перепутали количество.
 *
 * @param remote запросы корзины.
 * @param session сессия (идентификатор пользователя).
 */
class CartRepositoryImpl(
    private val remote: CartRemoteDataSource,
    private val session: SessionLocalDataSource,
) : CartRepository, UserDataCleaner {

    private val state = MutableStateFlow<List<CartItem>>(emptyList())
    private val mutex = Mutex()

    @Volatile
    private var loaded = false

    override val items: StateFlow<List<CartItem>> = state.asStateFlow()

    override val isLoaded: Boolean get() = loaded

    override suspend fun refresh(): AppResult<List<CartItem>> = mutex.withLock {
        val userId = userId() ?: return@withLock unauthorized()
        remote.cart(userId).map { items ->
            state.value = items
            loaded = true
            items
        }
    }

    override suspend fun add(product: Product): AppResult<Unit> = mutex.withLock {
        val userId = userId() ?: return@withLock unauthorized()
        val existing = state.value.firstOrNull { it.product.id == product.id }
        if (existing != null) {
            remote.updateCount(existing.id, existing.count + 1).map { replace(it) }
        } else {
            remote.add(userId, product.id, 1).map { item -> state.update { it + item } }
        }
    }

    override suspend fun removeProduct(productId: String): AppResult<Unit> {
        val item = state.value.firstOrNull { it.product.id == productId } ?: return AppResult.Success(Unit)
        return removeItem(item.id)
    }

    override suspend fun changeCount(itemId: String, count: Int): AppResult<Unit> = mutex.withLock {
        remote.updateCount(itemId, count).map { replace(it) }
    }

    override suspend fun removeItem(itemId: String): AppResult<Unit> = mutex.withLock {
        remote.remove(itemId).map { state.update { items -> items.filterNot { it.id == itemId } } }
    }

    override suspend fun clear(): AppResult<Unit> = mutex.withLock {
        for (item in state.value) {
            val result = remote.remove(item.id)
            if (result is AppResult.Failure) return@withLock result
            state.update { items -> items.filterNot { it.id == item.id } }
        }
        AppResult.Success(Unit)
    }

    override suspend fun clearUserData() {
        state.value = emptyList()
        loaded = false
    }

    private fun replace(item: CartItem) {
        state.update { items -> items.map { if (it.id == item.id) item else it } }
    }

    private suspend fun userId(): String? = session.load()?.userId

    private fun unauthorized() = AppResult.Failure(AppError.Unauthorized("Войдите в аккаунт"))
}
