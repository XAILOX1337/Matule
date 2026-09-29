package ru.matule.app.data.remote.network

import ru.matule.app.domain.model.CartItem
import ru.matule.app.domain.model.Category
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.model.Project
import ru.matule.app.domain.model.Promotion
import ru.matule.network.model.CartItemDto
import ru.matule.network.model.CategoryDto
import ru.matule.network.model.ProductDto
import ru.matule.network.model.ProjectDto
import ru.matule.network.model.PromotionDto
import java.time.Instant

/** Товар сервера → товар приложения. */
fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title,
    description = description,
    price = price.toInt(),
    type = type,
    consumption = consumption,
    categoryId = category,
)

/** Категория сервера → категория приложения. */
fun CategoryDto.toDomain(): Category = Category(id = id, title = title)

/**
 * Акция сервера → акция приложения.
 *
 * @param imageUrl полный адрес картинки или `null`.
 */
fun PromotionDto.toDomain(imageUrl: String?): Promotion = Promotion(
    id = id,
    title = title,
    price = price.toInt(),
    colorHex = color,
    imageUrl = imageUrl,
)

/** Позиция корзины; `null`, если сервер не вернул товар позиции. */
fun CartItemDto.toDomainOrNull(): CartItem? {
    val productDto = expand?.product ?: return null
    return CartItem(id = id, product = productDto.toDomain(), count = count.toInt().coerceAtLeast(1))
}

/**
 * Проект сервера → проект приложения.
 *
 * @param imageUrl полный адрес фото или `null`.
 */
fun ProjectDto.toDomain(imageUrl: String?): Project = Project(
    id = id,
    title = title,
    type = type,
    startDate = startDate,
    endDate = endDate,
    recipient = recipient,
    source = source,
    category = category,
    imageUrl = imageUrl,
    createdAtMillis = parsePocketBaseDate(created) ?: System.currentTimeMillis(),
)

/** «2026-09-29 10:00:00.000Z» → миллисекунды; `null`, если дата не читается. */
internal fun parsePocketBaseDate(value: String): Long? =
    runCatching { Instant.parse(value.trim().replace(' ', 'T')).toEpochMilli() }.getOrNull()
