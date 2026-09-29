package ru.matule.uikit.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.snackbar.MatuleSnackBarHost
import ru.matule.uikit.components.snackbar.rememberMatuleSnackBarState
import ru.matule.uikit.showcase.sections.ButtonsSection
import ru.matule.uikit.showcase.sections.CardsSection
import ru.matule.uikit.showcase.sections.ColorsSection
import ru.matule.uikit.showcase.sections.ControlsSection
import ru.matule.uikit.showcase.sections.HeaderSection
import ru.matule.uikit.showcase.sections.IconsSection
import ru.matule.uikit.showcase.sections.InputsSection
import ru.matule.uikit.showcase.sections.LogoSection
import ru.matule.uikit.showcase.sections.MenuSection
import ru.matule.uikit.showcase.sections.ModalSection
import ru.matule.uikit.showcase.sections.PinSection
import ru.matule.uikit.showcase.sections.SearchSection
import ru.matule.uikit.showcase.sections.SelectsSection
import ru.matule.uikit.showcase.sections.SnackBarSection
import ru.matule.uikit.showcase.sections.TabBarSection
import ru.matule.uikit.showcase.sections.TypographySection
import ru.matule.uikit.theme.MatuleTheme

/**
 * Главный экран демо: все секции UIKit в том же порядке, что в макете.
 */
@Composable
fun ShowcaseScreen() {
    val snackBarState = rememberMatuleSnackBarState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatuleTheme.colors.white),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Column(modifier = Modifier.statusBarsPadding().padding(20.dp)) {
                    Text(text = "UI Kit", style = MatuleTheme.typography.title1ExtraBold)
                    Text(
                        text = "Все компоненты библиотеки Matule и их состояния",
                        style = MatuleTheme.typography.textRegular,
                        color = MatuleTheme.colors.description,
                    )
                }
            }
            item { LogoSection() }
            item { ColorsSection() }
            item { TypographySection() }
            item { IconsSection() }
            item { InputsSection() }
            item { SelectsSection() }
            item { ModalSection() }
            item { SearchSection() }
            item { ButtonsSection() }
            item { ControlsSection() }
            item { CardsSection() }
            item { HeaderSection() }
            item { TabBarSection() }
            item { SnackBarSection(snackBarState) }
            item { PinSection() }
            item { MenuSection() }
        }
        MatuleSnackBarHost(
            state = snackBarState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        )
    }
}
