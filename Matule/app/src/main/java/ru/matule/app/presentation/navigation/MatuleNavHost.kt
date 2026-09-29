package ru.matule.app.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ru.matule.app.presentation.auth.login.LoginScreen
import ru.matule.app.presentation.auth.password.CreatePasswordScreen
import ru.matule.app.presentation.auth.profile.CreateProfileScreen
import ru.matule.app.presentation.main.cart.CartScreen
import ru.matule.app.presentation.main.catalog.CatalogScreen
import ru.matule.app.presentation.main.common.MainTab
import ru.matule.app.presentation.main.document.PrivacyPolicyScreen
import ru.matule.app.presentation.main.document.UserAgreementScreen
import ru.matule.app.presentation.main.home.HomeScreen
import ru.matule.app.presentation.main.profile.ProfileScreen
import ru.matule.app.presentation.main.projects.create.CreateProjectScreen
import ru.matule.app.presentation.main.projects.details.ProjectScreen
import ru.matule.app.presentation.main.projects.list.ProjectsScreen
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
    val toLogin: () -> Unit = { navController.openStack(listOf(Routes.LOGIN)) }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
        enterTransition = { fadeIn(tween(TRANSITION_MS)) },
        exitTransition = { fadeOut(tween(TRANSITION_MS)) },
        popEnterTransition = { fadeIn(tween(TRANSITION_MS)) },
        popExitTransition = { fadeOut(tween(TRANSITION_MS)) },
    ) {
        // ---------- запуск и вход ----------
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
            PinEnterScreen(onUnlocked = { stack -> navController.openStack(stack) }, onLoggedOut = toLogin)
        }

        // ---------- вкладки ----------
        composable(Routes.HOME) {
            HomeScreen(onTabSelected = openTab)
        }
        composable(Routes.CATALOG) {
            CatalogScreen(
                onTabSelected = openTab,
                onOpenCart = { navController.navigate(Routes.CART) { launchSingleTop = true } },
            )
        }
        composable(Routes.PROJECTS) {
            ProjectsScreen(
                onTabSelected = openTab,
                onCreateProject = { navController.navigate(Routes.PROJECT_CREATE) { launchSingleTop = true } },
                onOpenProject = { project -> navController.navigate(Routes.project(project.id)) { launchSingleTop = true } },
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onTabSelected = openTab,
                onOpenPrivacyPolicy = { navController.navigate(Routes.PRIVACY_POLICY) { launchSingleTop = true } },
                onOpenUserAgreement = { navController.navigate(Routes.USER_AGREEMENT) { launchSingleTop = true } },
                onLoggedOut = toLogin,
            )
        }

        // ---------- экраны внутри разделов ----------
        composable(Routes.CART) {
            CartScreen(onBack = back, onOrderPlaced = { navController.navigateToTab(Routes.HOME) })
        }
        composable(Routes.PROJECT_CREATE) {
            CreateProjectScreen(onTabSelected = openTab, onCreated = back)
        }
        composable(
            route = Routes.PROJECT,
            arguments = listOf(navArgument(Routes.PROJECT_ID) { type = NavType.StringType }),
        ) { entry ->
            ProjectScreen(projectId = entry.arguments?.getString(Routes.PROJECT_ID).orEmpty(), onBack = back)
        }
        composable(Routes.PRIVACY_POLICY) { PrivacyPolicyScreen(onBack = back) }
        composable(Routes.USER_AGREEMENT) { UserAgreementScreen(onBack = back) }
    }
}
