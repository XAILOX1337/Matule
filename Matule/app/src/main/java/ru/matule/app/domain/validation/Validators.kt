package ru.matule.app.domain.validation

import java.time.DateTimeException
import java.time.LocalDate

/**
 * Проверка email по заданию: `name@domenname.ru`, где имя и домен — только строчные латинские буквы и цифры.
 */
object EmailValidator {
    private val pattern = Regex("^[a-z0-9]+@[a-z0-9]+\\.[a-z]{2,}$")

    /** @return `true`, если email соответствует шаблону. */
    fun isValid(email: String): Boolean = pattern.matches(email)
}

/**
 * Результат проверки пароля по требованиям задания.
 *
 * @property hasMinLength не короче 8 символов.
 * @property hasUppercase есть заглавная буква.
 * @property hasLowercase есть строчная буква.
 * @property hasDigit есть цифра.
 * @property hasSpecial есть спецсимвол или пробел.
 */
data class PasswordCheck(
    val hasMinLength: Boolean,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
    val hasSpecial: Boolean,
) {
    /** Пароль надёжный — выполнены все требования. */
    val isValid: Boolean
        get() = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecial

    /** Невыполненные требования, человеческим языком. */
    val problems: List<String>
        get() = buildList {
            if (!hasMinLength) add("не менее 8 символов")
            if (!hasUppercase) add("заглавную букву")
            if (!hasLowercase) add("строчную букву")
            if (!hasDigit) add("цифру")
            if (!hasSpecial) add("спецсимвол или пробел")
        }
}

/**
 * Требования к надёжному паролю: не менее 8 символов, заглавные и строчные буквы, цифры,
 * спецсимволы (пробел тоже считается). Пример из задания: `oNQZnz$Hx2`.
 */
object PasswordValidator {
    /** Минимальная длина пароля. */
    const val MIN_LENGTH = 8

    /** @return результат проверки каждого требования. */
    fun check(password: String): PasswordCheck = PasswordCheck(
        hasMinLength = password.length >= MIN_LENGTH,
        hasUppercase = password.any { it.isUpperCase() },
        hasLowercase = password.any { it.isLowerCase() },
        hasDigit = password.any { it.isDigit() },
        hasSpecial = password.any { !it.isLetterOrDigit() },
    )
}

/**
 * Даты в формате дд.мм.гггг, которые вводятся цифрами через маску.
 */
object DateValidator {
    /**
     * Проверяет, что 8 цифр образуют существующую дату между 1900 и 2100 годом.
     *
     * @param digits цифры даты, например «28021991».
     * @return `true` для корректной даты.
     */
    fun isValidDigits(digits: String): Boolean = toLocalDate(digits) != null

    /**
     * @param digits цифры даты.
     * @return дата или `null`, если цифры не образуют корректную дату.
     */
    fun toLocalDate(digits: String): LocalDate? {
        if (digits.length != 8 || !digits.all { it.isDigit() }) return null
        val day = digits.substring(0, 2).toInt()
        val month = digits.substring(2, 4).toInt()
        val year = digits.substring(4, 8).toInt()
        if (year !in 1900..2100) return null
        return try {
            LocalDate.of(year, month, day)
        } catch (e: DateTimeException) {
            null
        }
    }

    /** «28021991» → «28.02.1991». */
    fun format(digits: String): String =
        if (digits.length == 8) "${digits.substring(0, 2)}.${digits.substring(2, 4)}.${digits.substring(4)}" else digits

    /** «28.02.1991» → «28021991». */
    fun toDigits(formatted: String): String = formatted.filter { it.isDigit() }
}
