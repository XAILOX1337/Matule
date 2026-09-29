package ru.matule.network.impl.filter

/**
 * Построение фильтров PocketBase (`?filter=...`) с экранированием пользовательского ввода.
 */
internal object PbFilter {

    /**
     * Убирает из значения символы, которые ломают синтаксис фильтра.
     *
     * @param value пользовательский ввод.
     * @return безопасное значение.
     */
    fun escape(value: String): String = value.replace("\\", "").replace("\"", "").replace("'", "").trim()

    /**
     * Условие равенства: `field = "value"`.
     *
     * @param field поле записи.
     * @param value значение.
     * @return условие фильтра.
     */
    fun eq(field: String, value: String): String = "$field = \"${escape(value)}\""

    /**
     * Условие «содержит» (без учёта регистра латиницы): `field ~ "value"`.
     *
     * @param field поле записи.
     * @param value подстрока.
     * @return условие фильтра.
     */
    fun contains(field: String, value: String): String = "$field ~ \"${escape(value)}\""

    /**
     * Объединяет условия через И, пустые и `null` пропускаются.
     *
     * @param parts условия.
     * @return общий фильтр или `null`, если условий нет.
     */
    fun and(vararg parts: String?): String? = join(" && ", parts)

    /**
     * Объединяет условия через ИЛИ, пустые и `null` пропускаются.
     *
     * @param parts условия.
     * @return общий фильтр или `null`, если условий нет.
     */
    fun or(vararg parts: String?): String? = join(" || ", parts)

    /**
     * Условие поиска товара: совпадение в названии (как ввели и с заглавной буквы —
     * SQLite не различает регистр только у латиницы) или в описании.
     *
     * @param query поисковый запрос.
     * @return условие фильтра или `null` для пустого запроса.
     */
    fun productSearch(query: String): String? {
        val text = escape(query)
        if (text.isEmpty()) return null
        val capitalized = text.replaceFirstChar { it.uppercaseChar() }
        val lower = text.lowercase()
        return or(
            contains("title", text),
            contains("title", capitalized),
            contains("title", lower),
            contains("description", lower),
        )
    }

    private fun join(separator: String, parts: Array<out String?>): String? {
        val valid = parts.filterNotNull().filter { it.isNotBlank() }.distinct()
        return when (valid.size) {
            0 -> null
            1 -> valid.first()
            else -> valid.joinToString(separator) { "($it)" }
        }
    }
}
