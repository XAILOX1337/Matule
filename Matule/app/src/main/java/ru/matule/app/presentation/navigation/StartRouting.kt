package ru.matule.app.presentation.navigation

import ru.matule.app.domain.model.StartState

/**
 * Куда вести пользователя при запуске. Результат — стек экранов: последний открывается,
 * остальные лежат под ним и доступны кнопкой «назад».
 */
object StartRouting {

    /** Стек после заставки. */
    fun afterSplash(state: StartState): List<String> = when {
        state.hasSession && state.hasPin -> listOf(Routes.PIN_ENTER)
        state.hasSession -> listOf(Routes.PIN_CREATE)
        else -> authBackStack(state)
    }

    /** Стек основного раздела, в конце — последний открытый экран. */
    fun mainBackStack(lastScreen: String?): List<String> {
        val pattern = lastScreen?.let(Routes::patternOf)
        if (lastScreen == null || pattern == null || pattern !in Routes.mainRestorable || pattern == Routes.HOME) {
            return listOf(Routes.HOME)
        }
        val parent = Routes.parents[pattern]
        return listOfNotNull(Routes.HOME, parent, lastScreen)
    }

    /** Регистрация продолжается с того же шага, если приложение закрыли посередине. */
    private fun authBackStack(state: StartState): List<String> = when (state.lastScreen) {
        Routes.CREATE_PROFILE -> listOf(Routes.LOGIN, Routes.CREATE_PROFILE)
        Routes.CREATE_PASSWORD -> if (state.hasProfileDraft) {
            listOf(Routes.LOGIN, Routes.CREATE_PROFILE, Routes.CREATE_PASSWORD)
        } else {
            listOf(Routes.LOGIN, Routes.CREATE_PROFILE)
        }
        else -> listOf(Routes.LOGIN)
    }
}
