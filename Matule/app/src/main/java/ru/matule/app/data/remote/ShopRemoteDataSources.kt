package ru.matule.app.data.remote

import ru.matule.app.domain.model.CartItem
import ru.matule.app.domain.model.Category
import ru.matule.app.domain.model.ImageFile
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.model.Project
import ru.matule.app.domain.model.ProjectDraft
import ru.matule.app.domain.model.Promotion
import ru.matule.app.domain.result.AppResult

/**
 * Запросы каталога. Абстракция над сетевой библиотекой.
 */
interface CatalogRemoteDataSource {
    /** Получение акций. */
    suspend fun promotions(): AppResult<List<Promotion>>

    /** Категории каталога. */
    suspend fun categories(): AppResult<List<Category>>

    /** Получение каталога; `categoryId == null` — все описания. */
    suspend fun products(categoryId: String?): AppResult<List<Product>>

    /** Поиск. */
    suspend fun search(query: String, categoryId: String?): AppResult<List<Product>>

    /** Получение описания товара. */
    suspend fun product(productId: String): AppResult<Product>
}

/**
 * Запросы корзины. Абстракция над сетевой библиотекой.
 */
interface CartRemoteDataSource {
    /** Корзина пользователя. */
    suspend fun cart(userId: String): AppResult<List<CartItem>>

    /** Добавление в корзину. */
    suspend fun add(userId: String, productId: String, count: Int): AppResult<CartItem>

    /** Изменение количества. */
    suspend fun updateCount(itemId: String, count: Int): AppResult<CartItem>

    /** Удаление позиции. */
    suspend fun remove(itemId: String): AppResult<Unit>
}

/**
 * Запросы заказов. Абстракция над сетевой библиотекой.
 */
interface OrderRemoteDataSource {
    /** Оформление заказа. */
    suspend fun createOrder(userId: String, items: List<CartItem>): AppResult<Unit>
}

/**
 * Запросы проектов. Абстракция над сетевой библиотекой.
 */
interface ProjectRemoteDataSource {
    /** Список проектов. */
    suspend fun projects(userId: String): AppResult<List<Project>>

    /** Проект по идентификатору. */
    suspend fun project(projectId: String): AppResult<Project>

    /** Создание проекта с фото. */
    suspend fun create(userId: String, draft: ProjectDraft, image: ImageFile): AppResult<Project>
}
