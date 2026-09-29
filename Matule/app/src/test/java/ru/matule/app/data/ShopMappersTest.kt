package ru.matule.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.matule.app.data.remote.network.parsePocketBaseDate
import ru.matule.app.data.remote.network.toDomainOrNull
import ru.matule.network.model.CartExpandDto
import ru.matule.network.model.CartItemDto
import ru.matule.network.model.ProductDto

/**
 * Преобразование ответов сервера в модели приложения.
 */
class ShopMappersTest {

    @Test
    fun pocketBaseDate_isParsed() {
        assertEquals(1_790_676_000_000L, parsePocketBaseDate("2026-09-29 10:00:00.000Z"))
        assertNull(parsePocketBaseDate("вчера"))
    }

    @Test
    fun cartItem_withoutExpandedProduct_isSkipped() {
        assertNull(CartItemDto(id = "c1", product = "p1").toDomainOrNull())
    }

    @Test
    fun cartItem_mapsProductAndCount() {
        val dto = CartItemDto(
            id = "c1",
            product = "p1",
            count = 2.0,
            expand = CartExpandDto(ProductDto(id = "p1", title = "Шорты", price = 300.0)),
        )
        val item = dto.toDomainOrNull()!!
        assertEquals(600, item.total)
        assertEquals("Шорты", item.product.title)
    }
}
