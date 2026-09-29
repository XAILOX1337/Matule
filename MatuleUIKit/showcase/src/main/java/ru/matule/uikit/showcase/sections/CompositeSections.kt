package ru.matule.uikit.showcase.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import ru.matule.uikit.components.button.MatuleButton
import ru.matule.uikit.components.button.MatuleButtonStyle
import ru.matule.uikit.components.card.MatuleCard
import ru.matule.uikit.components.card.MatuleCartCard
import ru.matule.uikit.components.card.MatuleProductCard
import ru.matule.uikit.components.card.MatuleProjectCard
import ru.matule.uikit.components.card.MatulePromoCard
import ru.matule.uikit.components.controls.MatuleToggle
import ru.matule.uikit.components.divider.MatuleDivider
import ru.matule.uikit.components.header.MatuleHeader
import ru.matule.uikit.components.header.MatuleHeaderStyle
import ru.matule.uikit.components.misc.MatuleImagePicker
import ru.matule.uikit.components.misc.MatuleMenuItem
import ru.matule.uikit.components.pin.MatuleNumPad
import ru.matule.uikit.components.pin.MatulePinDots
import ru.matule.uikit.components.snackbar.MatuleSnackBar
import ru.matule.uikit.components.snackbar.MatuleSnackBarState
import ru.matule.uikit.components.tabbar.MatuleTabBar
import ru.matule.uikit.components.tabbar.MatuleTabItem
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.showcase.ShowcaseSection
import ru.matule.uikit.showcase.ShowcaseState
import ru.matule.uikit.theme.MatuleTheme

/** Карточки: товар, корзина, проект, акция и основа карточки. */
@Composable
fun CardsSection() {
    ShowcaseSection("Карточки") {
        var inCart by remember { mutableStateOf(false) }
        var count by remember { mutableIntStateOf(1) }
        ShowcaseState("Card background") {
            MatuleCard(modifier = Modifier.fillMaxWidth().height(80.dp)) {}
        }
        ShowcaseState("Primary · не в корзине / в корзине (нажмите кнопку)") {
            MatuleProductCard(
                title = "Рубашка Воскресенье для машинного вязания",
                category = "Мужская одежда",
                price = "300 ₽",
                inCart = inCart,
                onAddClick = { inCart = true },
                onRemoveClick = { inCart = false },
            )
        }
        ShowcaseState("Primary · в корзине") {
            MatuleProductCard(
                title = "Шорты Вторник для машинного вязания",
                category = "Мужская одежда",
                price = "300 ₽",
                inCart = true,
                onAddClick = {},
                onRemoveClick = {},
            )
        }
        ShowcaseState("Cart") {
            MatuleCartCard(
                title = "Рубашка воскресенье для машинного вязания",
                price = "300 ₽",
                countText = "$count шт.",
                count = count,
                onIncrement = { count++ },
                onDecrement = { count-- },
                onRemove = {},
            )
        }
        ShowcaseState("Project") {
            MatuleProjectCard(title = "Мой первый проект", subtitle = "Прошло 2 дня", onAction = {})
        }
        ShowcaseState("Баннер акции") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    MatulePromoCard(
                        title = "Шорты Вторник",
                        price = "4000 ₽",
                        background = Brush.verticalGradient(listOf(Color(0xFF6CCFDD), Color(0xFF9FE3EA))),
                    )
                }
                item {
                    MatulePromoCard(
                        title = "Рубашка Воскресенье",
                        price = "8000 ₽",
                        background = Brush.verticalGradient(listOf(Color(0xFF4D8DF5), Color(0xFF8EBBFA))),
                    )
                }
            }
        }
    }
}

/** Хэдеры. */
@Composable
fun HeaderSection() {
    ShowcaseSection("Хэдер") {
        ShowcaseState("Large · назад и удалить") {
            MatuleHeader(title = "Корзина", onBack = {}, actionIcon = MatuleIcons.Trash, onAction = {})
        }
        ShowcaseState("Large · только заголовок") {
            MatuleHeader(title = "Корзина")
        }
        ShowcaseState("Compact · назад и удалить") {
            MatuleHeader(
                title = "Корзина",
                style = MatuleHeaderStyle.Compact,
                onBack = {},
                actionIcon = MatuleIcons.Trash,
                onAction = {},
            )
        }
        ShowcaseState("Compact · заголовок и «+»") {
            MatuleHeader(title = "Проекты", style = MatuleHeaderStyle.Compact, actionIcon = MatuleIcons.Plus, onAction = {})
        }
    }
}

private val tabs = listOf(
    MatuleTabItem("Главная", MatuleIcons.Home),
    MatuleTabItem("Каталог", MatuleIcons.Catalog),
    MatuleTabItem("Проекты", MatuleIcons.Projects),
    MatuleTabItem("Профиль", MatuleIcons.Profile),
)

/** Таббар: четыре варианта активной вкладки и интерактивный. */
@Composable
fun TabBarSection() {
    ShowcaseSection("Tabbar") {
        tabs.indices.forEach { index ->
            ShowcaseState("Активна «${tabs[index].title}»") {
                MatuleTabBar(items = tabs, selectedIndex = index, onSelect = {})
            }
        }
        var selected by remember { mutableIntStateOf(0) }
        ShowcaseState("Интерактивный") {
            MatuleTabBar(items = tabs, selectedIndex = selected, onSelect = { selected = it })
        }
    }
}

/** Уведомления. */
@Composable
fun SnackBarSection(state: MatuleSnackBarState) {
    ShowcaseSection("SnackBar") {
        ShowcaseState("Статичное") {
            MatuleSnackBar(title = "Произошла ошибка", message = "Ну вот опять", onClose = {})
        }
        ShowcaseState("Ошибка — висит, пока не закроют кнопкой") {
            MatuleButton(
                text = "Показать ошибку",
                onClick = { state.show("Произошла ошибка", "Нет соединения с сервером") },
                style = MatuleButtonStyle.Secondary,
            )
        }
        ShowcaseState("Успех — скрывается через 5 секунд") {
            MatuleButton(
                text = "Показать успех",
                onClick = { state.show("Заказ оформлен", "Скоро вернём вас на главную", durationMillis = 5_000) },
                style = MatuleButtonStyle.Secondary,
            )
        }
    }
}

/** PIN: точки во всех состояниях и клавиатура. */
@Composable
fun PinSection() {
    ShowcaseSection("PIN-код") {
        ShowcaseState("Точки · 0 / 2 / 4 / ошибка") {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                MatulePinDots(filledCount = 0)
                MatulePinDots(filledCount = 2)
                MatulePinDots(filledCount = 4)
                MatulePinDots(filledCount = 4, isError = true)
            }
        }
        var pin by remember { mutableStateOf("") }
        LaunchedEffect(pin) {
            if (pin.length == 4) {
                delay(600)
                pin = ""
            }
        }
        ShowcaseState("Клавиатура (нажатая клавиша синяя)") {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                MatulePinDots(filledCount = pin.length)
                androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
                MatuleNumPad(
                    onDigit = { digit -> if (pin.length < 4) pin += digit },
                    onBackspace = { pin = pin.dropLast(1) },
                )
            }
        }
    }
}

/** Меню профиля, плитка фото, разделитель. */
@Composable
fun MenuSection() {
    ShowcaseSection("Меню и прочее") {
        var notifications by remember { mutableStateOf(true) }
        ShowcaseState("Пункт меню со смайликом") {
            MatuleMenuItem(title = "Мои заказы", emoji = "📋", onClick = {})
        }
        ShowcaseState("Пункт меню с переключателем") {
            MatuleMenuItem(
                title = "Уведомления",
                emoji = "⚙️",
                trailing = { MatuleToggle(checked = notifications, onCheckedChange = { notifications = it }) },
            )
        }
        ShowcaseState("Плитка фото · пустая / ошибка") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MatuleImagePicker(onClick = {}, size = 120.dp)
                MatuleImagePicker(onClick = {}, size = 120.dp, error = true)
            }
        }
        ShowcaseState("Divider") { MatuleDivider() }
        Text(
            text = "Конец демо",
            style = MatuleTheme.typography.captionRegular,
            color = MatuleTheme.colors.placeholder,
        )
    }
}
