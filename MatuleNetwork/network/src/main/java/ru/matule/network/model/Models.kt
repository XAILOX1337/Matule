package ru.matule.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ авторизации PocketBase.
 *
 * @property token токен для заголовка Authorization.
 * @property record данные пользователя.
 */
@Serializable
data class AuthResponseDto(
    val token: String,
    val record: UserDto,
)

/**
 * Пользователь (коллекция `users`).
 *
 * @property id идентификатор записи.
 * @property email почта.
 * @property name имя.
 * @property patronymic отчество.
 * @property surname фамилия.
 * @property birthday дата рождения в формате дд.мм.гггг.
 * @property gender пол.
 * @property notifications включены ли уведомления.
 * @property avatar имя файла аватара (может быть пустым).
 */
@Serializable
data class UserDto(
    val id: String,
    val email: String = "",
    val name: String = "",
    val patronymic: String = "",
    val surname: String = "",
    val birthday: String = "",
    val gender: String = "",
    val notifications: Boolean = false,
    val avatar: String = "",
)

/**
 * Данные регистрации нового пользователя.
 *
 * @property email почта (логин).
 * @property password пароль.
 * @property passwordConfirm повтор пароля.
 * @property name имя.
 * @property patronymic отчество.
 * @property surname фамилия.
 * @property birthday дата рождения в формате дд.мм.гггг.
 * @property gender пол.
 * @property notifications включены ли уведомления (по умолчанию включены).
 * @property emailVisibility видна ли почта в ответах сервера.
 */
@Serializable
data class SignUpRequestDto(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val name: String,
    val patronymic: String,
    val surname: String,
    val birthday: String,
    val gender: String,
    val notifications: Boolean = true,
    val emailVisibility: Boolean = true,
)

/**
 * Изменение профиля: отправляются только заполненные (не `null`) поля.
 *
 * @property name имя.
 * @property patronymic отчество.
 * @property surname фамилия.
 * @property birthday дата рождения.
 * @property gender пол.
 * @property notifications включены ли уведомления.
 */
@Serializable
data class UpdateProfileRequestDto(
    val name: String? = null,
    val patronymic: String? = null,
    val surname: String? = null,
    val birthday: String? = null,
    val gender: String? = null,
    val notifications: Boolean? = null,
)

/**
 * Акция или новость (коллекция `promotions`).
 *
 * @property id идентификатор записи.
 * @property title название.
 * @property price цена в рублях.
 * @property color цвет баннера в формате #RRGGBB.
 * @property image имя файла картинки (может быть пустым).
 */
@Serializable
data class PromotionDto(
    val id: String,
    val title: String = "",
    val price: Double = 0.0,
    val color: String = "",
    val image: String = "",
)

/**
 * Категория описаний (коллекция `categories`).
 *
 * @property id идентификатор записи.
 * @property title название.
 * @property sort порядок сортировки.
 */
@Serializable
data class CategoryDto(
    val id: String,
    val title: String = "",
    val sort: Double = 0.0,
)

/**
 * Описание для машинного вязания — товар каталога (коллекция `products`).
 *
 * @property id идентификатор записи.
 * @property title название.
 * @property description полное описание.
 * @property price цена в рублях.
 * @property type тип, например «Мужская одежда».
 * @property consumption примерный расход пряжи, например «80-90 г».
 * @property category идентификатор категории.
 */
@Serializable
data class ProductDto(
    val id: String,
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val type: String = "",
    val consumption: String = "",
    val category: String = "",
)

/**
 * Позиция корзины (коллекция `cart`).
 *
 * @property id идентификатор записи.
 * @property user идентификатор пользователя.
 * @property product идентификатор товара.
 * @property count количество.
 * @property expand связанные записи, если запрошены через `expand=product`.
 */
@Serializable
data class CartItemDto(
    val id: String,
    val user: String = "",
    val product: String = "",
    val count: Double = 1.0,
    val expand: CartExpandDto? = null,
)

/**
 * Развёрнутые связи позиции корзины.
 *
 * @property product товар позиции.
 */
@Serializable
data class CartExpandDto(
    val product: ProductDto? = null,
)

/**
 * Позиция заказа.
 *
 * @property productId идентификатор товара.
 * @property title название товара на момент заказа.
 * @property price цена за штуку.
 * @property count количество.
 */
@Serializable
data class OrderItemDto(
    @SerialName("product_id") val productId: String,
    val title: String,
    val price: Int,
    val count: Int,
)

/**
 * Данные нового заказа.
 *
 * @property user идентификатор пользователя.
 * @property items позиции заказа.
 * @property total сумма заказа в рублях.
 */
@Serializable
data class CreateOrderRequestDto(
    val user: String,
    val items: List<OrderItemDto>,
    val total: Int,
)

/**
 * Созданный заказ (коллекция `orders`).
 *
 * @property id идентификатор заказа.
 * @property user идентификатор пользователя.
 * @property total сумма заказа.
 * @property created дата создания.
 */
@Serializable
data class OrderDto(
    val id: String,
    val user: String = "",
    val total: Double = 0.0,
    val created: String = "",
)

/**
 * Проект пользователя (коллекция `projects`).
 *
 * @property id идентификатор записи.
 * @property user идентификатор пользователя.
 * @property title название проекта.
 * @property type тип проекта.
 * @property startDate дата начала, дд.мм.гггг.
 * @property endDate дата окончания, дд.мм.гггг.
 * @property recipient для кого проект.
 * @property source источник описания (ссылка).
 * @property category категория.
 * @property image имя файла фото.
 * @property created дата создания в формате PocketBase («2026-09-29 10:00:00.000Z»).
 */
@Serializable
data class ProjectDto(
    val id: String,
    val user: String = "",
    val title: String = "",
    val type: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    val recipient: String = "",
    val source: String = "",
    val category: String = "",
    val image: String = "",
    val created: String = "",
)

/**
 * Данные нового проекта (отправляются формой multipart вместе с фото).
 *
 * @property userId идентификатор пользователя.
 * @property title название проекта.
 * @property type тип проекта.
 * @property startDate дата начала.
 * @property endDate дата окончания.
 * @property recipient для кого проект.
 * @property source источник описания.
 * @property category категория.
 */
data class CreateProjectRequestDto(
    val userId: String,
    val title: String,
    val type: String,
    val startDate: String,
    val endDate: String,
    val recipient: String,
    val source: String,
    val category: String,
)

/**
 * Файл изображения для загрузки на сервер.
 *
 * @property fileName имя файла, например `photo.jpg`.
 * @property mimeType MIME-тип, например `image/jpeg`.
 * @property bytes содержимое файла.
 */
class ImageUpload(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
)
