package ru.matule.app.presentation.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

/**
 * Открывает стек экранов с чистого листа: всё, что было в навигации, удаляется.
 * Экраны, которых нет в графе, пропускаются.
 *
 * @param routes экраны снизу вверх.
 */
fun NavController.openStack(routes: List<String>) {
    val existing = routes.filter { route -> graph.findNode(Routes.patternOf(route) ?: route) != null }
    val stack = existing.ifEmpty { listOf(Routes.HOME) }
    stack.forEachIndexed { index, route ->
        navigate(route) {
            if (index == 0) popUpTo(graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
}

/**
 * Переключение вкладки нижнего меню: у каждой вкладки своя история, «назад» ведёт на Главную.
 *
 * @param route маршрут вкладки.
 */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Конкретный маршрут записи стека: для экрана проекта подставляется идентификатор. */
fun NavBackStackEntry.concreteRoute(): String? {
    val pattern = destination.route ?: return null
    return if (pattern == Routes.PROJECT) {
        arguments?.getString(Routes.PROJECT_ID)?.let(Routes::project)
    } else {
        pattern
    }
}
