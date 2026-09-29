package ru.matule.app.domain.validation

/**
 * Имена полей форм для ошибок проверки. По ним экран понимает, под каким полем показать текст ошибки.
 * Совпадают с именами полей на сервере, поэтому ошибки сервера тоже попадают под нужное поле.
 */
object InputField {
    /** Почта. */
    const val EMAIL = "email"

    /** Пароль. */
    const val PASSWORD = "password"

    /** Повтор пароля. */
    const val PASSWORD_REPEAT = "passwordConfirm"

    /** Дата рождения. */
    const val BIRTHDAY = "birthday"
}
