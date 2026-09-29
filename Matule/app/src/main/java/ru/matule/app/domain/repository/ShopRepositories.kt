package ru.matule.app.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.matule.app.domain.model.CartItem
import ru.matule.app.domain.model.Category
import ru.matule.app.domain.model.HomeData
import ru.matule.app.domain.model.ImageFile
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.model.Project
import ru.matule.app.domain.model.ProjectDraft
import ru.matule.app.domain.result.AppResult

/**
 * Каталог описаний: главная страница (с кэшем), список, поиск, карточка товара.
 */
interface CatalogRepository {
    /** Главная из кэша или `null`, если её ещё не загружали. */
    suspend fun cachedHome(): HomeData?

    /** Главная с сервера; при успехе кэш обновляется. */
    suspend fun loadHome(): AppResult<HomeData>

    /** Категории каталога: с сервера, а без сети — из кэша главной. */
    suspend fun categories(): AppResult<List<Category>>

    /** Описания категории; `null` — все. */
    suspend fun products(categoryId: String?): AppResult<List<Product>>

    /** Поиск описаний. */
    suspend fun search(query: String, categoryId: String?): AppResult<List<Product>>

    /** Полное описание товара. */
    suspend fun product(productId: String): AppResult<Product>
}

/**
 * Корзина пользователя. Состояние общее для всех экранов: изменения сразу видны
 * на Главной, в Каталоге и в Корзине.
 */
interface CartRepository {
    /** Текущие позиции корзины. */
    val items: StateFlow<List<CartItem>>

    /** Загружена ли корзина с сервера в этом сеансе. */
    val isLoaded: Boolean

    /** Загрузить корзину с сервера. */
    suspend fun refresh(): AppResult<List<CartItem>>

    /** Добавить товар; если он уже в корзине — увеличить количество. */
    suspend fun add(product: Product): AppResult<Unit>

    /** Убрать товар из корзины. */
    suspend fun removeProduct(productId: String): AppResult<Unit>

    /** Изменить количество в позиции. */
    suspend fun changeCount(itemId: String, count: Int): AppResult<Unit>

    /** Удалить позицию. */
    suspend fun removeItem(itemId: String): AppResult<Unit>

    /** Очистить корзину. */
    suspend fun clear(): AppResult<Unit>
}

/**
 * Заказы.
 */
interface OrderRepository {
    /** Оформить заказ из позиций корзины. */
    suspend fun placeOrder(items: List<CartItem>): AppResult<Unit>
}

/**
 * Проекты пользователя.
 */
interface ProjectRepository {
    /** Загруженные проекты (новые первыми). */
    val projects: StateFlow<List<Project>>

    /** Загрузить проекты с сервера. */
    suspend fun refresh(): AppResult<List<Project>>

    /** Проект по идентификатору (из загруженных или с сервера). */
    suspend fun project(projectId: String): AppResult<Project>

    /** Создать проект с фото. */
    suspend fun create(draft: ProjectDraft, image: ImageFile): AppResult<Project>
}

/**
 * Фото проекта на устройстве: выбранная или снятая картинка сжимается и сохраняется в файлы приложения.
 */
interface ProjectImageRepository {
    /**
     * Сохраняет картинку из галереи или камеры.
     *
     * @param sourceUri адрес картинки (content:// или file://).
     * @return путь к подготовленному файлу.
     */
    suspend fun importImage(sourceUri: String): AppResult<String>

    /** Файл для отправки на сервер или `null`, если его уже нет. */
    suspend fun load(path: String): ImageFile?
}
