package ru.matule.app.data.remote.network

import ru.matule.app.data.remote.CartRemoteDataSource
import ru.matule.app.data.remote.CatalogRemoteDataSource
import ru.matule.app.data.remote.OrderRemoteDataSource
import ru.matule.app.data.remote.ProjectRemoteDataSource
import ru.matule.app.domain.model.CartItem
import ru.matule.app.domain.model.Category
import ru.matule.app.domain.model.ImageFile
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.model.Project
import ru.matule.app.domain.model.ProjectDraft
import ru.matule.app.domain.model.Promotion
import ru.matule.app.domain.result.AppError
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.validation.DateValidator
import ru.matule.network.MatuleApi
import ru.matule.network.model.CartItemDto
import ru.matule.network.model.CreateOrderRequestDto
import ru.matule.network.model.CreateProjectRequestDto
import ru.matule.network.model.ImageUpload
import ru.matule.network.model.OrderItemDto
import ru.matule.network.result.NetworkResult

/** Имена коллекций PocketBase для адресов файлов. */
private const val PROMOTIONS = "promotions"
private const val PROJECTS = "projects"

/**
 * Каталог через библиотеку MatuleNetwork.
 *
 * @param api клиент сервера.
 */
class NetworkCatalogRemoteDataSource(private val api: MatuleApi) : CatalogRemoteDataSource {

    override suspend fun promotions(): AppResult<List<Promotion>> =
        api.catalog.getPromotions().toAppResult { list ->
            list.map { dto -> dto.toDomain(dto.image.takeIf { it.isNotBlank() }?.let { api.fileUrl(PROMOTIONS, dto.id, it) }) }
        }

    override suspend fun categories(): AppResult<List<Category>> =
        api.catalog.getCategories().toAppResult { list -> list.map { it.toDomain() } }

    override suspend fun products(categoryId: String?): AppResult<List<Product>> =
        api.catalog.getProducts(categoryId).toAppResult { list -> list.map { it.toDomain() } }

    override suspend fun search(query: String, categoryId: String?): AppResult<List<Product>> =
        api.catalog.searchProducts(query, categoryId).toAppResult { list -> list.map { it.toDomain() } }

    override suspend fun product(productId: String): AppResult<Product> =
        api.catalog.getProduct(productId).toAppResult { it.toDomain() }
}

/**
 * Корзина через библиотеку MatuleNetwork.
 *
 * @param api клиент сервера.
 */
class NetworkCartRemoteDataSource(private val api: MatuleApi) : CartRemoteDataSource {

    override suspend fun cart(userId: String): AppResult<List<CartItem>> =
        api.cart.getCart(userId).toAppResult { list -> list.mapNotNull { it.toDomainOrNull() } }

    override suspend fun add(userId: String, productId: String, count: Int): AppResult<CartItem> =
        api.cart.addToCart(userId, productId, count).toCartItem()

    override suspend fun updateCount(itemId: String, count: Int): AppResult<CartItem> =
        api.cart.updateCount(itemId, count).toCartItem()

    override suspend fun remove(itemId: String): AppResult<Unit> =
        api.cart.removeFromCart(itemId).toAppResult { }

    private fun NetworkResult<CartItemDto>.toCartItem(): AppResult<CartItem> =
        when (this) {
            is NetworkResult.Failure -> AppResult.Failure(error.toAppError())
            is NetworkResult.Success -> data.toDomainOrNull()?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.Server("Сервер не вернул товар корзины"))
        }
}

/**
 * Заказы через библиотеку MatuleNetwork.
 *
 * @param api клиент сервера.
 */
class NetworkOrderRemoteDataSource(private val api: MatuleApi) : OrderRemoteDataSource {

    override suspend fun createOrder(userId: String, items: List<CartItem>): AppResult<Unit> {
        val request = CreateOrderRequestDto(
            user = userId,
            items = items.map { OrderItemDto(productId = it.product.id, title = it.product.title, price = it.product.price, count = it.count) },
            total = items.sumOf { it.total },
        )
        return api.orders.createOrder(request).toAppResult { }
    }
}

/**
 * Проекты через библиотеку MatuleNetwork.
 *
 * @param api клиент сервера.
 */
class NetworkProjectRemoteDataSource(private val api: MatuleApi) : ProjectRemoteDataSource {

    override suspend fun projects(userId: String): AppResult<List<Project>> =
        api.projects.getProjects(userId).toAppResult { list -> list.map { it.toDomain(imageUrl(it.id, it.image)) } }

    override suspend fun project(projectId: String): AppResult<Project> =
        api.projects.getProject(projectId).toAppResult { it.toDomain(imageUrl(it.id, it.image)) }

    override suspend fun create(userId: String, draft: ProjectDraft, image: ImageFile): AppResult<Project> {
        val request = CreateProjectRequestDto(
            userId = userId,
            title = draft.title,
            type = draft.type,
            startDate = DateValidator.format(draft.startDigits),
            endDate = DateValidator.format(draft.endDigits),
            recipient = draft.recipient,
            source = draft.source,
            category = draft.category,
        )
        val upload = ImageUpload(fileName = image.fileName, mimeType = image.mimeType, bytes = image.bytes)
        return api.projects.createProject(request, upload).toAppResult { it.toDomain(imageUrl(it.id, it.image)) }
    }

    private fun imageUrl(projectId: String, fileName: String): String? =
        fileName.takeIf { it.isNotBlank() }?.let { api.fileUrl(PROJECTS, projectId, it) }
}
