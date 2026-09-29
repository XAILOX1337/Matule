package ru.matule.app.data.local

import kotlinx.serialization.Serializable
import ru.matule.app.domain.model.ProfileDraft
import ru.matule.app.domain.model.ProjectDraft
import ru.matule.app.domain.repository.UserDataCleaner

/** Черновик регистрации в хранилище. */
@Serializable
private data class StoredProfileDraft(
    val name: String = "",
    val patronymic: String = "",
    val surname: String = "",
    val birthdayDigits: String = "",
    val gender: String = "",
    val email: String = "",
)

/** Черновик проекта в хранилище. */
@Serializable
private data class StoredProjectDraft(
    val type: String = "",
    val title: String = "",
    val startDigits: String = "",
    val endDigits: String = "",
    val recipient: String = "",
    val source: String = "",
    val category: String = "",
    val imagePath: String? = null,
)

/**
 * Состояние экранов между запусками: последний экран, черновики форм, почта на входе, фильтр каталога.
 *
 * @param store хранилище настроек.
 */
class AppStateLocalDataSource(private val store: PreferencesStore) : UserDataCleaner {

    /** Последний открытый экран. */
    suspend fun lastScreen(): String? = store.getString(KEY_LAST_SCREEN)

    /** Запоминает последний экран; `null` — забыть. */
    suspend fun saveLastScreen(route: String?) = store.putString(KEY_LAST_SCREEN, route)

    /** Черновик регистрации. */
    suspend fun profileDraft(): ProfileDraft? =
        store.getJson(KEY_PROFILE_DRAFT, StoredProfileDraft.serializer())?.let {
            ProfileDraft(it.name, it.patronymic, it.surname, it.birthdayDigits, it.gender, it.email)
        }

    /** Сохраняет черновик регистрации. */
    suspend fun saveProfileDraft(draft: ProfileDraft) = store.putJson(
        KEY_PROFILE_DRAFT,
        StoredProfileDraft.serializer(),
        StoredProfileDraft(draft.name, draft.patronymic, draft.surname, draft.birthdayDigits, draft.gender, draft.email),
    )

    /** Удаляет черновик регистрации. */
    suspend fun clearProfileDraft() = store.remove(KEY_PROFILE_DRAFT)

    /** Почта, введённая на экране входа. */
    suspend fun loginEmail(): String = store.getString(KEY_LOGIN_EMAIL).orEmpty()

    /** Сохраняет почту с экрана входа. */
    suspend fun saveLoginEmail(email: String) = store.putString(KEY_LOGIN_EMAIL, email)

    /** Черновик проекта. */
    suspend fun projectDraft(): ProjectDraft? =
        store.getJson(KEY_PROJECT_DRAFT, StoredProjectDraft.serializer())?.let {
            ProjectDraft(it.type, it.title, it.startDigits, it.endDigits, it.recipient, it.source, it.category, it.imagePath)
        }

    /** Сохраняет черновик проекта. */
    suspend fun saveProjectDraft(draft: ProjectDraft) = store.putJson(
        KEY_PROJECT_DRAFT,
        StoredProjectDraft.serializer(),
        StoredProjectDraft(
            draft.type, draft.title, draft.startDigits, draft.endDigits,
            draft.recipient, draft.source, draft.category, draft.imagePath,
        ),
    )

    /** Удаляет черновик проекта. */
    suspend fun clearProjectDraft() = store.remove(KEY_PROJECT_DRAFT)

    /** Сохраняет поиск и категорию каталога. */
    suspend fun saveCatalogFilter(query: String, categoryId: String?) =
        store.putStrings(mapOf(KEY_CATALOG_QUERY to query, KEY_CATALOG_CATEGORY to categoryId))

    /** Поиск и категория каталога. */
    suspend fun catalogFilter(): Pair<String, String?> =
        store.getString(KEY_CATALOG_QUERY).orEmpty() to store.getString(KEY_CATALOG_CATEGORY)

    /** При выходе забываем экраны и черновики пользователя; почта для входа остаётся. */
    override suspend fun clearUserData() =
        store.remove(KEY_LAST_SCREEN, KEY_PROJECT_DRAFT, KEY_CATALOG_QUERY, KEY_CATALOG_CATEGORY)

    private companion object {
        const val KEY_LAST_SCREEN = "last_screen"
        const val KEY_PROFILE_DRAFT = "profile_draft"
        const val KEY_LOGIN_EMAIL = "login_email"
        const val KEY_PROJECT_DRAFT = "project_draft"
        const val KEY_CATALOG_QUERY = "catalog_query"
        const val KEY_CATALOG_CATEGORY = "catalog_category"
    }
}
