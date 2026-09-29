package ru.matule.network.impl.api

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import ru.matule.network.model.AuthResponseDto
import ru.matule.network.model.CartItemDto
import ru.matule.network.model.CategoryDto
import ru.matule.network.model.CreateOrderRequestDto
import ru.matule.network.model.OrderDto
import ru.matule.network.model.ProductDto
import ru.matule.network.model.ProjectDto
import ru.matule.network.model.PromotionDto
import ru.matule.network.model.SignUpRequestDto
import ru.matule.network.model.UpdateProfileRequestDto
import ru.matule.network.model.UserDto

/**
 * Страница списка PocketBase.
 *
 * @property page номер страницы.
 * @property perPage размер страницы.
 * @property totalItems всего записей.
 * @property totalPages всего страниц.
 * @property items записи страницы.
 */
@Serializable
internal data class PageDto<T>(
    val page: Int = 1,
    val perPage: Int = 0,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val items: List<T> = emptyList(),
)

/**
 * Тело запроса авторизации.
 *
 * @property identity почта пользователя.
 * @property password пароль.
 */
@Serializable
internal data class AuthWithPasswordBody(val identity: String, val password: String)

/**
 * Тело запроса проверки почты.
 *
 * @property email почта.
 */
@Serializable
internal data class CheckEmailBody(val email: String)

/**
 * Ответ проверки почты.
 *
 * @property available свободна ли почта.
 */
@Serializable
internal data class CheckEmailResponse(val available: Boolean = false)

/**
 * Тело запроса добавления в корзину.
 *
 * @property user идентификатор пользователя.
 * @property product идентификатор товара.
 * @property count количество.
 */
@Serializable
internal data class CartCreateBody(val user: String, val product: String, val count: Int)

/**
 * Тело запроса изменения количества.
 *
 * @property count новое количество.
 */
@Serializable
internal data class CartUpdateBody(val count: Int)

/** Запросы авторизации (коллекция `users` и хук выхода). */
internal interface AuthApi {
    /** Вход по паролю. @param body почта и пароль. */
    @POST("api/collections/users/auth-with-password")
    suspend fun authWithPassword(@Body body: AuthWithPasswordBody): AuthResponseDto

    /** Обновление токена (нужен заголовок Authorization). */
    @POST("api/collections/users/auth-refresh")
    suspend fun authRefresh(): AuthResponseDto

    /** Проверка, свободна ли почта (хук `pb_hooks/matule.pb.js`). @param body почта. */
    @POST("api/matule/check-email")
    suspend fun checkEmail(@Body body: CheckEmailBody): CheckEmailResponse

    /** Выход: инвалидирует токены пользователя (хук `pb_hooks/matule.pb.js`). */
    @POST("api/matule/logout")
    suspend fun logout(): Response<Unit>
}

/** Записи коллекции `users`. */
internal interface UsersApi {
    /** Создание пользователя. @param body данные регистрации. */
    @POST("api/collections/users/records")
    suspend fun create(@Body body: SignUpRequestDto): UserDto

    /** Получение пользователя. @param id идентификатор. */
    @GET("api/collections/users/records/{id}")
    suspend fun get(@Path("id") id: String): UserDto

    /** Изменение пользователя. @param id идентификатор. @param body изменяемые поля. */
    @PATCH("api/collections/users/records/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateProfileRequestDto): UserDto
}

/** Каталог: акции, категории, товары. */
internal interface CatalogApi {
    /** Список акций. @param perPage размер страницы. @param sort сортировка. */
    @GET("api/collections/promotions/records")
    suspend fun promotions(
        @Query("perPage") perPage: Int = 50,
        @Query("sort") sort: String = "sort",
    ): PageDto<PromotionDto>

    /** Список категорий. @param perPage размер страницы. @param sort сортировка. */
    @GET("api/collections/categories/records")
    suspend fun categories(
        @Query("perPage") perPage: Int = 50,
        @Query("sort") sort: String = "sort",
    ): PageDto<CategoryDto>

    /** Список товаров. @param filter фильтр PocketBase; `null` — без фильтра. @param perPage размер страницы. @param sort сортировка. */
    @GET("api/collections/products/records")
    suspend fun products(
        @Query("filter") filter: String? = null,
        @Query("perPage") perPage: Int = 200,
        @Query("sort") sort: String = "sort,title",
    ): PageDto<ProductDto>

    /** Один товар. @param id идентификатор. */
    @GET("api/collections/products/records/{id}")
    suspend fun product(@Path("id") id: String): ProductDto
}

/** Записи коллекции `cart`. */
internal interface CartApi {
    /** Корзина с товарами. @param filter фильтр по пользователю. @param expand какие связи развернуть. @param perPage размер страницы. @param sort сортировка. */
    @GET("api/collections/cart/records")
    suspend fun list(
        @Query("filter") filter: String,
        @Query("expand") expand: String = "product",
        @Query("perPage") perPage: Int = 200,
        @Query("sort") sort: String = "created",
    ): PageDto<CartItemDto>

    /** Добавление позиции. @param body пользователь, товар, количество. @param expand какие связи развернуть. */
    @POST("api/collections/cart/records")
    suspend fun create(
        @Body body: CartCreateBody,
        @Query("expand") expand: String = "product",
    ): CartItemDto

    /** Изменение количества. @param id позиция. @param body новое количество. @param expand какие связи развернуть. */
    @PATCH("api/collections/cart/records/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: CartUpdateBody,
        @Query("expand") expand: String = "product",
    ): CartItemDto

    /** Удаление позиции. @param id позиция. */
    @DELETE("api/collections/cart/records/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}

/** Записи коллекции `orders`. */
internal interface OrdersApi {
    /** Создание заказа. @param body позиции и сумма. */
    @POST("api/collections/orders/records")
    suspend fun create(@Body body: CreateOrderRequestDto): OrderDto
}

/** Записи коллекции `projects`. */
internal interface ProjectsApi {
    /** Проекты пользователя. @param filter фильтр по пользователю. @param perPage размер страницы. @param sort сортировка. */
    @GET("api/collections/projects/records")
    suspend fun list(
        @Query("filter") filter: String,
        @Query("perPage") perPage: Int = 200,
        @Query("sort") sort: String = "-created",
    ): PageDto<ProjectDto>

    /** Один проект. @param id идентификатор. */
    @GET("api/collections/projects/records/{id}")
    suspend fun get(@Path("id") id: String): ProjectDto

    /** Создание проекта формой multipart. @param fields текстовые поля. @param image файл фото или `null`. */
    @Multipart
    @POST("api/collections/projects/records")
    suspend fun create(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?,
    ): ProjectDto
}
