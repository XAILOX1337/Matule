package ru.matule.network

import ru.matule.network.config.NetworkConfig
import ru.matule.network.config.TokenProvider
import ru.matule.network.impl.MatuleApiImpl
import ru.matule.network.service.AuthService
import ru.matule.network.service.CartService
import ru.matule.network.service.CatalogService
import ru.matule.network.service.OrderService
import ru.matule.network.service.ProfileService
import ru.matule.network.service.ProjectService

/**
 * Точка доступа к серверу Matule. Все запросы сгруппированы по сервисам;
 * наружу видны только интерфейсы, реализация скрыта внутри библиотеки.
 */
interface MatuleApi {
    /** Авторизация, регистрация, выход. */
    val auth: AuthService

    /** Профиль пользователя. */
    val profile: ProfileService

    /** Акции, категории, каталог, поиск. */
    val catalog: CatalogService

    /** Корзина. */
    val cart: CartService

    /** Заказы. */
    val orders: OrderService

    /** Проекты. */
    val projects: ProjectService

    /**
     * Полный адрес файла, загруженного в PocketBase.
     *
     * @param collection имя коллекции, например `projects`.
     * @param recordId идентификатор записи.
     * @param fileName имя файла из поля записи.
     * @return URL файла.
     */
    fun fileUrl(collection: String, recordId: String, fileName: String): String
}

/**
 * Фабрика сетевого клиента.
 */
object MatuleNetwork {
    /**
     * Создаёт клиент для работы с сервером.
     *
     * @param config адрес сервера, таймауты и логирование.
     * @param tokenProvider откуда брать токен для авторизованных запросов.
     * @return готовый к работе [MatuleApi].
     */
    fun create(config: NetworkConfig, tokenProvider: TokenProvider): MatuleApi =
        MatuleApiImpl(config, tokenProvider)
}
