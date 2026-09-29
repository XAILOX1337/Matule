package ru.matule.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.matule.app.domain.validation.DateValidator
import ru.matule.app.domain.validation.EmailValidator
import ru.matule.app.domain.validation.PasswordValidator

/**
 * Проверки из задания: формат почты, требования к паролю, даты.
 */
class ValidatorsTest {

    @Test
    fun email_matchesTaskPattern() {
        assertTrue(EmailValidator.isValid("name@domenname.ru"))
        assertTrue(EmailValidator.isValid("user2026@mail2.com"))
    }

    @Test
    fun email_rejectsUppercaseSymbolsAndMissingParts() {
        assertFalse(EmailValidator.isValid("Name@domenname.ru"))
        assertFalse(EmailValidator.isValid("name.surname@mail.ru"))
        assertFalse(EmailValidator.isValid("name@mail"))
        assertFalse(EmailValidator.isValid("@mail.ru"))
        assertFalse(EmailValidator.isValid("name@mail.r"))
        assertFalse(EmailValidator.isValid(""))
    }

    @Test
    fun password_exampleFromTask_isValid() {
        assertTrue(PasswordValidator.check("oNQZnz\$Hx2").isValid)
        assertTrue(PasswordValidator.check("Pass word1").isValid)
    }

    @Test
    fun password_reportsEveryMissingRule() {
        val check = PasswordValidator.check("abc")
        assertFalse(check.isValid)
        assertEquals(
            listOf("не менее 8 символов", "заглавную букву", "цифру", "спецсимвол или пробел"),
            check.problems,
        )
    }

    @Test
    fun date_validatesRealDates() {
        assertTrue(DateValidator.isValidDigits("28021991"))
        assertTrue(DateValidator.isValidDigits("29022024"))
        assertFalse(DateValidator.isValidDigits("29022023"))
        assertFalse(DateValidator.isValidDigits("31041991"))
        assertFalse(DateValidator.isValidDigits("2802199"))
        assertNull(DateValidator.toLocalDate("01011850"))
    }

    @Test
    fun date_formatsDigits() {
        assertEquals("28.02.1991", DateValidator.format("28021991"))
        assertEquals("28021991", DateValidator.toDigits("28.02.1991"))
    }
}
