package ru.matule.app.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.matule.app.domain.model.StartState
import ru.matule.app.presentation.navigation.Routes
import ru.matule.app.presentation.navigation.StartRouting

/**
 * Выбор первого экрана и восстановление последнего экрана после перезапуска.
 */
class StartRoutingTest {

    private fun state(
        hasSession: Boolean = false,
        hasPin: Boolean = false,
        lastScreen: String? = null,
        hasProfileDraft: Boolean = false,
    ) = StartState(hasSession, hasPin, lastScreen, hasProfileDraft)

    @Test
    fun authorizedUser_seesPinEnter() {
        assertEquals(listOf(Routes.PIN_ENTER), StartRouting.afterSplash(state(hasSession = true, hasPin = true)))
    }

    @Test
    fun signedInWithoutPin_createsPin() {
        assertEquals(listOf(Routes.PIN_CREATE), StartRouting.afterSplash(state(hasSession = true)))
    }

    @Test
    fun guest_seesLogin() {
        assertEquals(listOf(Routes.LOGIN), StartRouting.afterSplash(state()))
    }

    @Test
    fun registrationContinuesFromLastStep() {
        assertEquals(
            listOf(Routes.LOGIN, Routes.CREATE_PROFILE),
            StartRouting.afterSplash(state(lastScreen = Routes.CREATE_PROFILE, hasProfileDraft = true)),
        )
        assertEquals(
            listOf(Routes.LOGIN, Routes.CREATE_PROFILE, Routes.CREATE_PASSWORD),
            StartRouting.afterSplash(state(lastScreen = Routes.CREATE_PASSWORD, hasProfileDraft = true)),
        )
    }

    @Test
    fun mainBackStack_restoresLastScreenAboveItsParent() {
        assertEquals(listOf(Routes.HOME), StartRouting.mainBackStack(null))
        assertEquals(listOf(Routes.HOME), StartRouting.mainBackStack(Routes.HOME))
        assertEquals(listOf(Routes.HOME, Routes.PROFILE), StartRouting.mainBackStack(Routes.PROFILE))
        assertEquals(
            listOf(Routes.HOME, Routes.PROFILE, Routes.PRIVACY_POLICY),
            StartRouting.mainBackStack(Routes.PRIVACY_POLICY),
        )
        assertEquals(
            listOf(Routes.HOME, Routes.PROJECTS, "project/abc"),
            StartRouting.mainBackStack(Routes.project("abc")),
        )
    }

    @Test
    fun mainBackStack_ignoresAuthAndUnknownScreens() {
        assertEquals(listOf(Routes.HOME), StartRouting.mainBackStack(Routes.LOGIN))
        assertEquals(listOf(Routes.HOME), StartRouting.mainBackStack("unknown"))
        assertEquals(listOf(Routes.HOME), StartRouting.mainBackStack("project/"))
    }
}
