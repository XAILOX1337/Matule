package ru.matule.app.domain.repository

import ru.matule.app.domain.model.ProfileDraft
import ru.matule.app.domain.model.ProjectDraft
import ru.matule.app.domain.model.User
import ru.matule.app.domain.result.AppResult

/**
 * Авторизация и сессия пользователя.
 */
interface AuthRepository {
    /** Вход по почте и паролю; при успехе сессия сохраняется. */
    suspend fun signIn(email: String, password: String): AppResult<Unit>

    /** Свободна ли почта для регистрации (проверка на сервере). */
    suspend fun isEmailAvailable(email: String): AppResult<Boolean>

    /** Регистрация по данным черновика и паролю, затем вход. */
    suspend fun signUp(draft: ProfileDraft, password: String): AppResult<Unit>

    /** Обновление токена сохранённой сессии. */
    suspend fun refreshSession(): AppResult<Unit>

    /** Выход: запрос на сервер и очистка всех данных пользователя на устройстве. */
    suspend fun logout(): AppResult<Unit>

    /** Есть ли сохранённая сессия. */
    suspend fun hasSession(): Boolean

    /** Идентификатор текущего пользователя или `null`. */
    suspend fun currentUserId(): String?
}

/**
 * PIN-код для входа в приложение (хранится только на устройстве).
 */
interface PinRepository {
    /** Задан ли PIN. */
    suspend fun hasPin(): Boolean

    /** Сохраняет PIN. */
    suspend fun savePin(pin: String)

    /** Проверяет PIN. */
    suspend fun verifyPin(pin: String): Boolean
}

/**
 * Профиль пользователя.
 */
interface ProfileRepository {
    /** Профиль с сервера (при успехе кэшируется). */
    suspend fun getProfile(): AppResult<User>

    /** Профиль из кэша или `null`. */
    suspend fun cachedProfile(): User?

    /** Включены ли уведомления. */
    suspend fun isNotificationsEnabled(): Boolean

    /** Включает или выключает уведомления (локально и в профиле на сервере). */
    suspend fun setNotificationsEnabled(enabled: Boolean): AppResult<Unit>
}

/**
 * Состояние приложения между запусками: последний экран и черновики форм.
 */
interface AppStateRepository {
    /** Запоминает последний открытый экран. */
    suspend fun saveLastScreen(route: String?)

    /** Последний открытый экран. */
    suspend fun lastScreen(): String?

    /** Сохраняет черновик регистрации. */
    suspend fun saveProfileDraft(draft: ProfileDraft)

    /** Черновик регистрации или `null`. */
    suspend fun profileDraft(): ProfileDraft?

    /** Удаляет черновик регистрации. */
    suspend fun clearProfileDraft()

    /** Сохраняет введённую на экране входа почту. */
    suspend fun saveLoginEmail(email: String)

    /** Почта с экрана входа. */
    suspend fun loginEmail(): String

    /** Сохраняет черновик нового проекта. */
    suspend fun saveProjectDraft(draft: ProjectDraft)

    /** Черновик проекта или `null`. */
    suspend fun projectDraft(): ProjectDraft?

    /** Удаляет черновик проекта. */
    suspend fun clearProjectDraft()

    /** Сохраняет поиск и категорию каталога. */
    suspend fun saveCatalogFilter(query: String, categoryId: String?)

    /** Поиск и категория каталога. */
    suspend fun catalogFilter(): Pair<String, String?>
}

/**
 * Хранилище, которое нужно очищать при выходе пользователя.
 */
interface UserDataCleaner {
    /** Удаляет данные пользователя. */
    suspend fun clearUserData()
}
