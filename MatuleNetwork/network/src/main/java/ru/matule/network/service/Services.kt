package ru.matule.network.service

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

/**
 * Авторизация: вход, регистрация, обновление токена и выход.
 */
interface AuthService {
    /**
     * Авторизация по почте и паролю.
     *
     * @param email почта пользователя.
     * @param password пароль.
     * @return токен и данные пользователя.
     */
    suspend fun signIn(email: String, password: String): NetworkResult<AuthResponseDto>

    /**
     * Проверка, свободна ли почта для регистрации (хук `pb_hooks/matule.pb.js`).
     *
     * @param email почта.
     * @return `true`, если пользователя с такой почтой ещё нет.
     */
    suspend fun isEmailAvailable(email: String): NetworkResult<Boolean>

    /**
     * Создание пользователя (регистрация).
     *
     * @param request данные нового пользователя.
     * @return созданный пользователь (без токена — после регистрации нужно войти).
     */
    suspend fun signUp(request: SignUpRequestDto): NetworkResult<UserDto>

    /**
     * Обновление токена текущего пользователя.
     *
     * @return новый токен и актуальные данные пользователя.
     */
    suspend fun refresh(): NetworkResult<AuthResponseDto>

    /**
     * Выход: сервер делает недействительными все токены пользователя.
     *
     * @return успех или ошибка.
     */
    suspend fun logout(): NetworkResult<Unit>
}

/**
 * Профиль пользователя.
 */
interface ProfileService {
    /**
     * Получение информации о профиле.
     *
     * @param userId идентификатор пользователя.
     * @return данные пользователя.
     */
    suspend fun getProfile(userId: String): NetworkResult<UserDto>

    /**
     * Изменение профиля.
     *
     * @param userId идентификатор пользователя.
     * @param request изменяемые поля (`null` — не менять).
     * @return обновлённые данные пользователя.
     */
    suspend fun updateProfile(userId: String, request: UpdateProfileRequestDto): NetworkResult<UserDto>
}

/**
 * Каталог: акции, категории, описания и поиск.
 */
interface CatalogService {
    /**
     * Получение акций и новостей.
     *
     * @return акции в порядке сортировки.
     */
    suspend fun getPromotions(): NetworkResult<List<PromotionDto>>

    /**
     * Получение категорий каталога.
     *
     * @return категории в порядке сортировки.
     */
    suspend fun getCategories(): NetworkResult<List<CategoryDto>>

    /**
     * Получение каталога.
     *
     * @param categoryId идентификатор категории; `null` — все описания.
     * @return описания.
     */
    suspend fun getProducts(categoryId: String? = null): NetworkResult<List<ProductDto>>

    /**
     * Поиск описаний по названию и тексту.
     *
     * @param query поисковый запрос.
     * @param categoryId ограничить поиск категорией; `null` — искать везде.
     * @return найденные описания.
     */
    suspend fun searchProducts(query: String, categoryId: String? = null): NetworkResult<List<ProductDto>>

    /**
     * Получение описания товара.
     *
     * @param productId идентификатор товара.
     * @return полное описание товара.
     */
    suspend fun getProduct(productId: String): NetworkResult<ProductDto>
}

/**
 * Корзина пользователя.
 */
interface CartService {
    /**
     * Получение корзины вместе с товарами.
     *
     * @param userId идентификатор пользователя.
     * @return позиции корзины.
     */
    suspend fun getCart(userId: String): NetworkResult<List<CartItemDto>>

    /**
     * Добавление в корзину.
     *
     * @param userId идентификатор пользователя.
     * @param productId идентификатор товара.
     * @param count количество.
     * @return созданная позиция вместе с товаром.
     */
    suspend fun addToCart(userId: String, productId: String, count: Int = 1): NetworkResult<CartItemDto>

    /**
     * Изменение количества в позиции корзины.
     *
     * @param cartItemId идентификатор позиции.
     * @param count новое количество (не меньше 1).
     * @return обновлённая позиция вместе с товаром.
     */
    suspend fun updateCount(cartItemId: String, count: Int): NetworkResult<CartItemDto>

    /**
     * Удаление позиции из корзины.
     *
     * @param cartItemId идентификатор позиции.
     * @return успех или ошибка.
     */
    suspend fun removeFromCart(cartItemId: String): NetworkResult<Unit>
}

/**
 * Заказы.
 */
interface OrderService {
    /**
     * Оформление заказа.
     *
     * @param request позиции и сумма заказа.
     * @return созданный заказ.
     */
    suspend fun createOrder(request: CreateOrderRequestDto): NetworkResult<OrderDto>
}

/**
 * Проекты пользователя.
 */
interface ProjectService {
    /**
     * Список проектов, новые первыми.
     *
     * @param userId идентификатор пользователя.
     * @return проекты пользователя.
     */
    suspend fun getProjects(userId: String): NetworkResult<List<ProjectDto>>

    /**
     * Один проект.
     *
     * @param projectId идентификатор проекта.
     * @return проект.
     */
    suspend fun getProject(projectId: String): NetworkResult<ProjectDto>

    /**
     * Создание проекта.
     *
     * @param request данные проекта.
     * @param image фото проекта; `null` — без фото.
     * @return созданный проект.
     */
    suspend fun createProject(request: CreateProjectRequestDto, image: ImageUpload?): NetworkResult<ProjectDto>
}
