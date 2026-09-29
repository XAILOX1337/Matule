package ru.matule.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ru.matule.app.di.matuleViewModel
import ru.matule.app.domain.usecase.SaveLastScreenUseCase

/**
 * Запоминает последний открытый экран.
 *
 * @param saveLastScreen сохранение маршрута.
 */
class LastScreenViewModel(private val saveLastScreen: SaveLastScreenUseCase) : ViewModel() {

    /** Сохраняет маршрут. */
    fun save(route: String) {
        viewModelScope.launch { saveLastScreen(route) }
    }
}

/**
 * Следит за навигацией и сохраняет каждый экран, который можно восстановить после перезапуска.
 * Заставка и экраны PIN не сохраняются: с них всегда начинается запуск.
 *
 * @param navController контроллер навигации.
 */
@Composable
fun LastScreenSaver(navController: NavController) {
    val viewModel = matuleViewModel { LastScreenViewModel(saveLastScreen) }
    LaunchedEffect(navController, viewModel) {
        navController.currentBackStackEntryFlow.collect { entry ->
            val route = entry.concreteRoute() ?: return@collect
            if (Routes.patternOf(route) != null) viewModel.save(route)
        }
    }
}
