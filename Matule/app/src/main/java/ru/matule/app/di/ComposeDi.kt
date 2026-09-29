package ru.matule.app.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/** Контейнер зависимостей, доступный всем экранам. */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer не передан: оберните экран в CompositionLocalProvider(LocalAppContainer provides ...)")
}

/**
 * ViewModel экрана, собранная из зависимостей контейнера.
 * Живёт, пока экран находится в стеке навигации.
 *
 * @param key ключ, если на одном экране нужно несколько ViewModel одного типа.
 * @param create как собрать ViewModel из контейнера.
 */
@Composable
inline fun <reified VM : ViewModel> matuleViewModel(
    key: String? = null,
    crossinline create: AppContainer.() -> VM,
): VM {
    val container = LocalAppContainer.current
    return viewModel(key = key, factory = viewModelFactory { initializer { container.create() } })
}
