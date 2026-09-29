package ru.matule.app.domain.model

/**
 * Пользователь приложения.
 *
 * @property id идентификатор на сервере.
 * @property email почта.
 * @property name имя.
 * @property patronymic отчество.
 * @property surname фамилия.
 * @property birthday дата рождения, дд.мм.гггг.
 * @property gender пол.
 * @property notificationsEnabled включены ли уведомления.
 */
data class User(
    val id: String,
    val email: String,
    val name: String,
    val patronymic: String,
    val surname: String,
    val birthday: String,
    val gender: String,
    val notificationsEnabled: Boolean,
)

/**
 * Черновик регистрации — поля экрана «Создание профиля».
 * Сохраняется локально, чтобы пережить перезапуск приложения.
 *
 * @property name имя.
 * @property patronymic отчество.
 * @property surname фамилия.
 * @property birthdayDigits дата рождения только цифрами, например «28021991».
 * @property gender пол.
 * @property email почта.
 */
data class ProfileDraft(
    val name: String = "",
    val patronymic: String = "",
    val surname: String = "",
    val birthdayDigits: String = "",
    val gender: String = "",
    val email: String = "",
)

/**
 * Что известно о пользователе при запуске — по этому решается, какой экран открыть.
 *
 * @property hasSession сохранена ли сессия (пользователь авторизован).
 * @property hasPin задан ли PIN для входа в приложение.
 * @property lastScreen последний открытый экран (маршрут) или `null`.
 * @property hasProfileDraft есть ли начатая регистрация.
 */
data class StartState(
    val hasSession: Boolean,
    val hasPin: Boolean,
    val lastScreen: String?,
    val hasProfileDraft: Boolean,
)

/**
 * Акция или новость на главной.
 *
 * @property id идентификатор.
 * @property title название.
 * @property price цена в рублях.
 * @property colorHex цвет баннера, #RRGGBB.
 * @property imageUrl адрес картинки или `null`.
 */
data class Promotion(
    val id: String,
    val title: String,
    val price: Int,
    val colorHex: String,
    val imageUrl: String?,
)

/**
 * Категория описаний.
 *
 * @property id идентификатор.
 * @property title название.
 */
data class Category(
    val id: String,
    val title: String,
)

/**
 * Описание для машинного вязания (товар).
 *
 * @property id идентификатор.
 * @property title название.
 * @property description полное описание.
 * @property price цена в рублях.
 * @property type тип, например «Мужская одежда».
 * @property consumption примерный расход пряжи.
 * @property categoryId идентификатор категории.
 */
data class Product(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val type: String,
    val consumption: String,
    val categoryId: String,
)

/**
 * Данные главной страницы (кэшируются).
 *
 * @property promotions акции.
 * @property categories категории.
 * @property products описания.
 */
data class HomeData(
    val promotions: List<Promotion>,
    val categories: List<Category>,
    val products: List<Product>,
)

/**
 * Позиция корзины.
 *
 * @property id идентификатор позиции.
 * @property product товар.
 * @property count количество.
 */
data class CartItem(
    val id: String,
    val product: Product,
    val count: Int,
) {
    /** Стоимость позиции. */
    val total: Int get() = product.price * count
}

/**
 * Проект пользователя.
 *
 * @property id идентификатор.
 * @property title название.
 * @property type тип.
 * @property startDate дата начала, дд.мм.гггг.
 * @property endDate дата окончания, дд.мм.гггг.
 * @property recipient для кого.
 * @property source источник описания.
 * @property category категория.
 * @property imageUrl адрес фото или `null`.
 * @property createdAtMillis время создания.
 */
data class Project(
    val id: String,
    val title: String,
    val type: String,
    val startDate: String,
    val endDate: String,
    val recipient: String,
    val source: String,
    val category: String,
    val imageUrl: String?,
    val createdAtMillis: Long,
)

/**
 * Черновик нового проекта — поля экрана «Создать проект».
 *
 * @property type тип.
 * @property title название.
 * @property startDigits дата начала цифрами.
 * @property endDigits дата окончания цифрами.
 * @property recipient для кого.
 * @property source источник описания.
 * @property category категория.
 * @property imagePath путь к подготовленному фото или `null`.
 */
data class ProjectDraft(
    val type: String = "",
    val title: String = "",
    val startDigits: String = "",
    val endDigits: String = "",
    val recipient: String = "",
    val source: String = "",
    val category: String = "",
    val imagePath: String? = null,
)

/**
 * Файл изображения для отправки.
 *
 * @property fileName имя файла.
 * @property mimeType MIME-тип.
 * @property bytes содержимое.
 */
class ImageFile(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
)
