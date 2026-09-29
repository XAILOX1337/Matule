package ru.matule.app.presentation.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Форматирование чисел и дат для экранов.
 */
object Formatters {

    private val russian: Locale = Locale.forLanguageTag("ru")
    private val longDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", russian)

    /** 1991-02-28 → «28 февраля 1991». */
    fun longDate(date: LocalDate): String = longDateFormat.format(date)

    /** 690 → «690 ₽». */
    fun price(rubles: Int): String = "$rubles ₽"

    /** 1 → «1 штука», 3 → «3 штуки», 5 → «5 штук». */
    fun pieces(count: Int): String = "$count ${plural(count, "штука", "штуки", "штук")}"

    /**
     * Сколько прошло с момента создания: «Сегодня», «Прошло 2 дня».
     *
     * @param createdAtMillis время создания.
     * @param nowMillis текущее время.
     */
    fun elapsedSince(createdAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val days = TimeUnit.MILLISECONDS.toDays((nowMillis - createdAtMillis).coerceAtLeast(0))
        return if (days == 0L) "Сегодня" else "Прошло $days ${plural(days, "день", "дня", "дней")}"
    }

    /** Русское склонение слова после числа: 1 день, 2 дня, 5 дней. */
    fun plural(count: Long, one: String, few: String, many: String): String {
        val n = kotlin.math.abs(count) % 100
        val last = n % 10
        return when {
            n in 11L..14L -> many
            last == 1L -> one
            last in 2L..4L -> few
            else -> many
        }
    }

    /** То же для Int. */
    fun plural(count: Int, one: String, few: String, many: String): String = plural(count.toLong(), one, few, many)
}
