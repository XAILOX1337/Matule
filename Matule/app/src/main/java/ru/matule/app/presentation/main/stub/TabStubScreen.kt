package ru.matule.app.presentation.main.stub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.matule.app.presentation.common.ScreenTitle
import ru.matule.app.presentation.main.common.MainTab
import ru.matule.app.presentation.main.common.MainTabScaffold
import ru.matule.uikit.theme.MatuleTheme

/**
 * Временная вкладка: разделы магазина и проектов делаются в спринте 4.
 *
 * @param tab вкладка.
 * @param onTabSelected переход на другую вкладку.
 */
@Composable
fun TabStubScreen(tab: MainTab, onTabSelected: (MainTab) -> Unit) {
    MainTabScaffold(selected = tab, onTabSelected = onTabSelected) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 32.dp),
        ) {
            ScreenTitle(title = tab.item.title)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Раздел появится в следующем спринте",
                style = MatuleTheme.typography.textRegular,
                color = MatuleTheme.colors.description,
            )
        }
    }
}
