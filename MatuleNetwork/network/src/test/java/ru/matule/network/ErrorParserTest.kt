package ru.matule.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.matule.network.impl.client.NetworkJson
import ru.matule.network.impl.error.ErrorParser
import ru.matule.network.impl.filter.PbFilter
import ru.matule.network.result.NetworkError

/**
 * Разбор ошибок PocketBase и построение фильтров.
 */
class ErrorParserTest {

    private val parser = ErrorParser(NetworkJson)

    @Test
    fun requiredField_isTranslated() {
        val error = parser.parse(
            400,
            """{"status":400,"message":"Failed to create record.","data":{"name":{"code":"validation_required","message":"Missing required value."}}}""",
        )
        assertTrue(error is NetworkError.Validation)
        assertEquals("Заполните поле «Имя»", error.message)
        assertEquals("Заполните поле «Имя»", (error as NetworkError.Validation).fieldErrors["name"])
    }

    @Test
    fun oldPocketBaseFormat_withCodeField_isParsed() {
        val error = parser.parse(400, """{"code":400,"message":"Something went wrong.","data":{}}""")
        assertEquals("Something went wrong.", (error as NetworkError.Validation).serverMessage)
    }

    @Test
    fun emptyBody_doesNotCrash() {
        assertTrue(parser.parse(404, null) is NetworkError.NotFound)
        assertTrue(parser.parse(403, "") is NetworkError.Forbidden)
        assertTrue(parser.parse(502, "<html>Bad gateway</html>") is NetworkError.Server)
    }

    @Test
    fun filter_escapesQuotes() {
        assertEquals("title = \"abc\"", PbFilter.eq("title", "a\"b'c"))
    }

    @Test
    fun filter_joinsOnlyNonEmptyParts() {
        assertNull(PbFilter.and(null, ""))
        assertEquals("a = \"1\"", PbFilter.and(PbFilter.eq("a", "1"), null))
        assertEquals("(a = \"1\") && (b = \"2\")", PbFilter.and(PbFilter.eq("a", "1"), PbFilter.eq("b", "2")))
    }

    @Test
    fun productSearch_emptyQuery_isNull() {
        assertNull(PbFilter.productSearch("   "))
    }
}
