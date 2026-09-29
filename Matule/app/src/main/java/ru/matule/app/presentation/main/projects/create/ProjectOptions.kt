package ru.matule.app.presentation.main.projects.create

import ru.matule.uikit.components.select.MatuleSelectOption

/**
 * Варианты селектов экрана «Создать проект» (со смайликами, как требует задание).
 */
object ProjectOptions {

    /** Тип изделия. */
    val types: List<MatuleSelectOption> = listOf(
        option("Свитер", "🧶"),
        option("Кардиган", "🧥"),
        option("Платье", "👗"),
        option("Шапка", "🧢"),
        option("Шарф", "🧣"),
        option("Носки", "🧦"),
    )

    /** Для кого проект. */
    val recipients: List<MatuleSelectOption> = listOf(
        option("Себе", "🙋"),
        option("Ребёнку", "🧒"),
        option("Маме", "👩"),
        option("Папе", "👨"),
        option("Другу", "🤝"),
        option("В подарок", "🎁"),
    )

    /** Категория. */
    val categories: List<MatuleSelectOption> = listOf(
        option("Женщинам", "👩"),
        option("Мужчинам", "👨"),
        option("Детям", "🧒"),
        option("Аксессуары", "👜"),
    )

    /** Выбранный вариант по сохранённому названию. */
    fun find(options: List<MatuleSelectOption>, title: String): MatuleSelectOption? =
        options.firstOrNull { it.title == title }

    private fun option(title: String, emoji: String) = MatuleSelectOption(id = title, title = title, emoji = emoji)
}
