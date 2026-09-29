package ru.matule.app.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.matule.app.presentation.common.Formatters
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Форматирование цен, количества и дат.
 */
class FormattersTest {

    @Test
    fun pieces_usesRussianPlurals() {
        assertEquals("1 штука", Formatters.pieces(1))
        assertEquals("3 штуки", Formatters.pieces(3))
        assertEquals("5 штук", Formatters.pieces(5))
        assertEquals("11 штук", Formatters.pieces(11))
        assertEquals("22 штуки", Formatters.pieces(22))
    }

    @Test
    fun price_addsRubleSign() {
        assertEquals("690 ₽", Formatters.price(690))
    }

    @Test
    fun elapsedSince_countsDays() {
        val now = 1_000_000_000_000L
        assertEquals("Сегодня", Formatters.elapsedSince(now - TimeUnit.HOURS.toMillis(3), now))
        assertEquals("Прошло 2 дня", Formatters.elapsedSince(now - TimeUnit.DAYS.toMillis(2), now))
        assertEquals("Прошло 5 дней", Formatters.elapsedSince(now - TimeUnit.DAYS.toMillis(5), now))
    }

    @Test
    fun longDate_isRussian() {
        assertEquals("28 февраля 1991", Formatters.longDate(LocalDate.of(1991, 2, 28)))
    }
}
