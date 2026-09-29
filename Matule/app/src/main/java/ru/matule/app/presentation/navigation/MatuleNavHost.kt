package ru.matule.app.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.matule.app.presentation.auth.login.LoginScreen
import ru.matule.app.presentation.auth.password.CreatePasswordScreen
import ru.matule.app.presentation.auth.profile.CreateProfileScreen
import ru.matule.app.presentation.main.common.MainTab
import ru.matule.app.presentation.main.document.PrivacyPolicyScreen
import ru.matule.app.presentation.main.document.UserAgreementScreen
import ru.matule.app.presentation.main.profile.ProfileScreen
import ru.matule.app.presentation.main.stub.TabStubScreen
import ru.matule.app.presentation.pin.create.PinCreateScreen
import ru.matule.app.presentation.pin.enter.PinEnterScreen
import ru.matule.app.presentation.splash.SplashScreen

/** Длительность смены экранов. */
private const val TRANSITION_MS = 250

/**
 * Граф навигации приложения. Экраны ничего не знают друг о друге:
 * все переходы описаны здесь.
 *
 * @param navController контроллер навигации.
 * @param modifier модификатор.
 */
@Composable
fun MatuleNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val openTab: (MainTab) -> Unit = { tab -> navController.navigateToTab(tab.route) }
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
        enterTransition = { fadeIn(tween(TRANSITION_MS)) },
        exitTransition = { fadeOut(tween(TRANSITION_MS)) },
        popEnterTransition = { fadeIn(tween(TRANSITION_MS)) },
        popExitTransition = { fadeOut(tween(TRANSITION_MS)) },
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = { stack -> navController.openStack(stack) })
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onSignedIn = { navController.openStack(listOf(Routes.PIN_CREATE)) },
                onRegister = { navController.navigate(Routes.CREATE_PROFILE) { launchSingleTop = true } },
            )
        }
        composable(Routes.CREATE_PROFILE) {
            CreateProfileScreen(onNext = { navController.navigate(Routes.CREATE_PASSWORD) { launchSingleTop = true } })
        }
        composable(Routes.CREATE_PASSWORD) {
            CreatePasswordScreen(onRegistered = { navController.openStack(listOf(Routes.PIN_CREATE)) })
        }
        composable(Routes.PIN_CREATE) {
            PinCreateScreen(onCreated = { navController.openStack(listOf(Routes.HOME)) })
        }
        composable(Routes.PIN_ENTER) {
            PinEnterScreen(
                onUnlocked = { stack -> navController.openStack(stack) },
                onLoggedOut = { navController.openStack(listOf(Routes.LOGIN)) },
            )
        }

        composable(Routes.HOME) { TabStubScreen(tab = MainTab.Home, onTabSelected = openTab) }
        composable(Routes.CATALOG) { TabStubScreen(tab = MainTab.Catalog, onTabSelected = openTab) }
        composable(Routes.PROJECTS) { TabStubScreen(tab = MainTab.Projects, onTabSelected = openTab) }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onTabSelected = openTab,
                onOpenPrivacyPolicy = { navController.navigate(Routes.PRIVACY_POLICY) { launchSingleTop = true } },
                onOpenUserAgreement = { navController.navigate(Routes.USER_AGREEMENT) { launchSingleTop = true } },
                onLoggedOut = { navController.openStack(listOf(Routes.LOGIN)) },
            )
        }
        composable(Routes.PRIVACY_POLICY) { PrivacyPolicyScreen(onBack = back) }
        composable(Routes.USER_AGREEMENT) { UserAgreementScreen(onBack = back) }
    }
}
