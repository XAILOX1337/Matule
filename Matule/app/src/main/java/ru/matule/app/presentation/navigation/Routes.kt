package ru.matule.app.presentation.navigation

/**
 * Маршруты экранов. Маршрут — обычная строка: её можно сохранить в DataStore
 * и восстановить последний экран после перезапуска.
 */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val CREATE_PROFILE = "create_profile"
    const val CREATE_PASSWORD = "create_password"
    const val PIN_CREATE = "pin_create"
    const val PIN_ENTER = "pin_enter"

    const val HOME = "home"
    const val CATALOG = "catalog"
    const val PROJECTS = "projects"
    const val PROFILE = "profile"
    const val CART = "cart"
    const val PROJECT_CREATE = "project_create"
    const val PRIVACY_POLICY = "privacy_policy"
    const val USER_AGREEMENT = "user_agreement"

    /** Аргумент экрана проекта. */
    const val PROJECT_ID = "projectId"

    /** Экран проекта (шаблон маршрута). */
    const val PROJECT = "project/{$PROJECT_ID}"

    private const val PROJECT_PREFIX = "project/"

    /** Маршрут конкретного проекта. */
    fun project(projectId: String): String = PROJECT_PREFIX + projectId

    /** Экраны до входа, которые восстанавливаются после перезапуска. */
    val authRestorable: Set<String> = setOf(LOGIN, CREATE_PROFILE, CREATE_PASSWORD)

    /** Экраны после входа, которые восстанавливаются после ввода PIN. */
    val mainRestorable: Set<String> = setOf(
        HOME, CATALOG, PROJECTS, PROFILE, CART, PROJECT_CREATE, PROJECT, PRIVACY_POLICY, USER_AGREEMENT,
    )

    /** Откуда открывается экран — чтобы «назад» после восстановления вёл туда же, что и обычно. */
    val parents: Map<String, String> = mapOf(
        CART to CATALOG,
        PROJECT_CREATE to PROJECTS,
        PROJECT to PROJECTS,
        PRIVACY_POLICY to PROFILE,
        USER_AGREEMENT to PROFILE,
    )

    /**
     * Шаблон маршрута для конкретного маршрута: «project/abc» → «project/{projectId}».
     *
     * @return шаблон или `null`, если такого экрана нет.
     */
    fun patternOf(route: String): String? = when {
        route.startsWith(PROJECT_PREFIX) && route.length > PROJECT_PREFIX.length -> PROJECT
        route in authRestorable || route in mainRestorable -> route
        else -> null
    }
}
