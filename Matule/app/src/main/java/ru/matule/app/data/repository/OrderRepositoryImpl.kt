package ru.matule.app.data.repository

import ru.matule.app.data.local.SessionLocalDataSource
import ru.matule.app.data.remote.OrderRemoteDataSource
import ru.matule.app.domain.model.CartItem
import ru.matule.app.domain.repository.OrderRepository
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult

/**
 * Заказы.
 *
 * @param remote запросы заказов.
 * @param session сессия (идентификатор пользователя).
 */
class OrderRepositoryImpl(
    private val remote: OrderRemoteDataSource,
    private val session: SessionLocalDataSource,
) : OrderRepository {

    override suspend fun placeOrder(items: List<CartItem>): AppResult<Unit> {
        val userId = session.load()?.userId ?: return AppResult.Failure(AppError.Unauthorized("Войдите в аккаунт"))
        return remote.createOrder(userId, items)
    }
}
