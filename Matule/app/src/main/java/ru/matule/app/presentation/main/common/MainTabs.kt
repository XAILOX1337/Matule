package ru.matule.app.presentation.main.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.matule.app.presentation.navigation.Routes
import ru.matule.uikit.components.tabbar.MatuleTabBar
import ru.matule.uikit.components.tabbar.MatuleTabItem
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/**
 * Вкладки нижнего меню.
 *
 * @property route маршрут вкладки.
 * @property item подпись и иконка.
 */
enum class MainTab(val route: String, val item: MatuleTabItem) {
    Home(Routes.HOME, MatuleTabItem("Главная", MatuleIcons.Home)),
    Catalog(Routes.CATALOG, MatuleTabItem("Каталог", MatuleIcons.Catalog)),
    Projects(Routes.PROJECTS, MatuleTabItem("Проекты", MatuleIcons.Projects)),
    Profile(Routes.PROFILE, MatuleTabItem("Профиль", MatuleIcons.Profile)),
}

/**
 * Каркас экранов с нижним меню: контент сверху, TabBar из UIKit снизу.
 *
 * @param selected активная вкладка.
 * @param onTabSelected выбор вкладки.
 * @param modifier модификатор.
 * @param content содержимое вкладки (отступ от статус-бара уже учтён).
 */
@Composable
fun MainTabScaffold(
    selected: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val tabs = MainTab.entries
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatuleTheme.colors.white),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding(),
            content = content,
        )
        MatuleTabBar(
            items = tabs.map { it.item },
            selectedIndex = selected.ordinal,
            onSelect = { index -> onTabSelected(tabs[index]) },
        )
    }
}
