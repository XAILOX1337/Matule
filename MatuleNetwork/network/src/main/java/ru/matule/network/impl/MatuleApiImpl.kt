package ru.matule.network.impl

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.matule.network.MatuleApi
import ru.matule.network.config.NetworkConfig
import ru.matule.network.config.TokenProvider
import ru.matule.network.impl.api.AuthApi
import ru.matule.network.impl.api.AuthWithPasswordBody
import ru.matule.network.impl.api.CartApi
import ru.matule.network.impl.api.CartCreateBody
import ru.matule.network.impl.api.CartUpdateBody
import ru.matule.network.impl.api.CatalogApi
import ru.matule.network.impl.api.CheckEmailBody
import ru.matule.network.impl.api.OrdersApi
import ru.matule.network.impl.api.ProjectsApi
import ru.matule.network.impl.api.UsersApi
import ru.matule.network.impl.client.HttpClientFactory
import ru.matule.network.impl.client.NetworkJson
import ru.matule.network.impl.error.ErrorParser
import ru.matule.network.impl.error.SafeApiCaller
import ru.matule.network.impl.error.requireSuccess
import ru.matule.network.impl.filter.PbFilter
import ru.matule.network.model.AuthResponseDto
import ru.matule.network.model.CartItemDto
import ru.matule.network.model.CategoryDto
import ru.matule.network.model.CreateOrderRequestDto
import ru.matule.network.model.CreateProjectRequestDto
import ru.matule.network.model.ImageUpload
import ru.matule.network.model.OrderDto
import ru.matule.network.model.ProductDto
import ru.matule.network.model.ProjectDto
import ru.matule.network.model.PromotionDto
import ru.matule.network.model.SignUpRequestDto
import ru.matule.network.model.UpdateProfileRequestDto
import ru.matule.network.model.UserDto
import ru.matule.network.result.NetworkResult
import ru.matule.network.service.AuthService
import ru.matule.network.service.CartService
import ru.matule.network.service.CatalogService
import ru.matule.network.service.OrderService
import ru.matule.network.service.ProfileService
import ru.matule.network.service.ProjectService

/**
 * Реализация [MatuleApi] на Retrofit. Создаёт сервисы и общий [SafeApiCaller].
 *
 * @param config настройки клиента.
 * @param tokenProvider источник токена.
 */
internal class MatuleApiImpl(
    private val config: NetworkConfig,
    tokenProvider: TokenProvider,
) : MatuleApi {

    private val retrofit = HttpClientFactory(config, tokenProvider).createRetrofit()
    private val caller = SafeApiCaller(ErrorParser(NetworkJson))

    override val auth: AuthService =
        AuthServiceImpl(retrofit.create(AuthApi::class.java), retrofit.create(UsersApi::class.java), caller)
    override val profile: ProfileService = ProfileServiceImpl(retrofit.create(UsersApi::class.java), caller)
    override val catalog: CatalogService = CatalogServiceImpl(retrofit.create(CatalogApi::class.java), caller)
    override val cart: CartService = CartServiceImpl(retrofit.create(CartApi::class.java), caller)
    override val orders: OrderService = OrderServiceImpl(retrofit.create(OrdersApi::class.java), caller)
    override val projects: ProjectService = ProjectServiceImpl(retrofit.create(ProjectsApi::class.java), caller)

    override fun fileUrl(collection: String, recordId: String, fileName: String): String =
        "${config.normalizedBaseUrl}api/files/$collection/$recordId/$fileName"
}

/**
 * Авторизация.
 *
 * @param api запросы авторизации.
 * @param usersApi запросы коллекции пользователей.
 * @param caller безопасное выполнение запросов.
 */
internal class AuthServiceImpl(
    private val api: AuthApi,
    private val usersApi: UsersApi,
    private val caller: SafeApiCaller,
) : AuthService {
    override suspend fun signIn(email: String, password: String): NetworkResult<AuthResponseDto> =
        caller.call { api.authWithPassword(AuthWithPasswordBody(identity = email, password = password)) }

    override suspend fun isEmailAvailable(email: String): NetworkResult<Boolean> =
        caller.call { api.checkEmail(CheckEmailBody(email = email)).available }

    override suspend fun signUp(request: SignUpRequestDto): NetworkResult<UserDto> =
        caller.call { usersApi.create(request) }

    override suspend fun refresh(): NetworkResult<AuthResponseDto> =
        caller.call { api.authRefresh() }

    override suspend fun logout(): NetworkResult<Unit> =
        caller.call { api.logout().requireSuccess() }
}

/**
 * Профиль.
 *
 * @param api запросы коллекции пользователей.
 * @param caller безопасное выполнение запросов.
 */
internal class ProfileServiceImpl(
    private val api: UsersApi,
    private val caller: SafeApiCaller,
) : ProfileService {
    override suspend fun getProfile(userId: String): NetworkResult<UserDto> =
        caller.call { api.get(userId) }

    override suspend fun updateProfile(userId: String, request: UpdateProfileRequestDto): NetworkResult<UserDto> =
        caller.call { api.update(userId, request) }
}

/**
 * Каталог.
 *
 * @param api запросы каталога.
 * @param caller безопасное выполнение запросов.
 */
internal class CatalogServiceImpl(
    private val api: CatalogApi,
    private val caller: SafeApiCaller,
) : CatalogService {
    override suspend fun getPromotions(): NetworkResult<List<PromotionDto>> =
        caller.call { api.promotions().items }

    override suspend fun getCategories(): NetworkResult<List<CategoryDto>> =
        caller.call { api.categories().items }

    override suspend fun getProducts(categoryId: String?): NetworkResult<List<ProductDto>> =
        caller.call { api.products(filter = categoryId?.let { PbFilter.eq("category", it) }).items }

    override suspend fun searchProducts(query: String, categoryId: String?): NetworkResult<List<ProductDto>> {
        val filter = PbFilter.and(
            PbFilter.productSearch(query),
            categoryId?.let { PbFilter.eq("category", it) },
        )
        return caller.call { api.products(filter = filter).items }
    }

    override suspend fun getProduct(productId: String): NetworkResult<ProductDto> =
        caller.call { api.product(productId) }
}

/**
 * Корзина.
 *
 * @param api запросы корзины.
 * @param caller безопасное выполнение запросов.
 */
internal class CartServiceImpl(
    private val api: CartApi,
    private val caller: SafeApiCaller,
) : CartService {
    override suspend fun getCart(userId: String): NetworkResult<List<CartItemDto>> =
        caller.call { api.list(filter = PbFilter.eq("user", userId)).items }

    override suspend fun addToCart(userId: String, productId: String, count: Int): NetworkResult<CartItemDto> =
        caller.call { api.create(CartCreateBody(user = userId, product = productId, count = count)) }

    override suspend fun updateCount(cartItemId: String, count: Int): NetworkResult<CartItemDto> =
        caller.call { api.update(cartItemId, CartUpdateBody(count = count)) }

    override suspend fun removeFromCart(cartItemId: String): NetworkResult<Unit> =
        caller.call { api.delete(cartItemId).requireSuccess() }
}

/**
 * Заказы.
 *
 * @param api запросы заказов.
 * @param caller безопасное выполнение запросов.
 */
internal class OrderServiceImpl(
    private val api: OrdersApi,
    private val caller: SafeApiCaller,
) : OrderService {
    override suspend fun createOrder(request: CreateOrderRequestDto): NetworkResult<OrderDto> =
        caller.call { api.create(request) }
}

/**
 * Проекты.
 *
 * @param api запросы проектов.
 * @param caller безопасное выполнение запросов.
 */
internal class ProjectServiceImpl(
    private val api: ProjectsApi,
    private val caller: SafeApiCaller,
) : ProjectService {
    override suspend fun getProjects(userId: String): NetworkResult<List<ProjectDto>> =
        caller.call { api.list(filter = PbFilter.eq("user", userId)).items }

    override suspend fun createProject(request: CreateProjectRequestDto, image: ImageUpload?): NetworkResult<ProjectDto> {
        val textType = "text/plain".toMediaType()
        val fields: Map<String, RequestBody> = mapOf(
            "user" to request.userId,
            "title" to request.title,
            "type" to request.type,
            "start_date" to request.startDate,
            "end_date" to request.endDate,
            "recipient" to request.recipient,
            "source" to request.source,
            "category" to request.category,
        ).mapValues { (_, value) -> value.toRequestBody(textType) }
        val imagePart = image?.let {
            MultipartBody.Part.createFormData("image", it.fileName, it.bytes.toRequestBody(it.mimeType.toMediaType()))
        }
        return caller.call { api.create(fields, imagePart) }
    }
}
