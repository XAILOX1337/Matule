package ru.matule.uikit.showcase.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.matule.uikit.components.button.MatuleBubbleButton
import ru.matule.uikit.components.button.MatuleButton
import ru.matule.uikit.components.button.MatuleButtonStyle
import ru.matule.uikit.components.button.MatuleCartButton
import ru.matule.uikit.components.button.MatuleLoginButton
import ru.matule.uikit.components.button.MatuleSmallButton
import ru.matule.uikit.components.button.MatuleSmallButtonStyle
import ru.matule.uikit.components.button.MatuleTextButton
import ru.matule.uikit.components.chips.MatuleChip
import ru.matule.uikit.components.chips.MatuleChipRow
import ru.matule.uikit.components.controls.MatuleCounter
import ru.matule.uikit.components.controls.MatuleToggle
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.showcase.ShowcaseSection
import ru.matule.uikit.showcase.ShowcaseState
import ru.matule.uikit.theme.MatuleTheme

/** Все кнопки: большие, маленькие, чипы, корзина, вход через сервисы, bubble, ссылка. */
@Composable
fun ButtonsSection() {
    ShowcaseSection("Кнопки") {
        ShowcaseState("Big · Primary") { MatuleButton(text = "Подтвердить", onClick = {}) }
        ShowcaseState("Big · Primary неактивная") { MatuleButton(text = "Подтвердить", onClick = {}, enabled = false) }
        ShowcaseState("Big · Primary загрузка") { MatuleButton(text = "Подтвердить", onClick = {}, loading = true) }
        ShowcaseState("Big · Secondary") {
            MatuleButton(text = "Подтвердить", onClick = {}, style = MatuleButtonStyle.Secondary)
        }
        ShowcaseState("Big · Secondary неактивная") {
            MatuleButton(text = "Подтвердить", onClick = {}, style = MatuleButtonStyle.Secondary, enabled = false)
        }
        ShowcaseState("Big · Tertiary") {
            MatuleButton(text = "Подтвердить", onClick = {}, style = MatuleButtonStyle.Tertiary)
        }
        ShowcaseState("Small · Primary / Outlined / неактивная / Tertiary") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MatuleSmallButton(text = "Добавить", onClick = {})
                MatuleSmallButton(text = "Убрать", onClick = {}, style = MatuleSmallButtonStyle.Outlined)
                MatuleSmallButton(text = "Добавить", onClick = {}, enabled = false)
                MatuleSmallButton(text = "Подтвердить", onClick = {}, style = MatuleSmallButtonStyle.Tertiary)
            }
        }
        ShowcaseState("Chips · активный / неактивный") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MatuleChip(text = "Популярные", selected = true, onClick = {})
                MatuleChip(text = "Популярные", selected = false, onClick = {})
            }
        }
        ShowcaseState("Категории меню (ряд чипов с прокруткой)") {
            var selected by remember { mutableIntStateOf(0) }
            MatuleChipRow(
                items = listOf("Популярные", "Женщинам", "Мужчинам", "Детям", "Аксессуары"),
                selectedIndex = selected,
                onSelect = { selected = it },
                contentPadding = PaddingValues(0.dp),
            )
        }
        ShowcaseState("Cart") { MatuleCartButton(price = "500 ₽", onClick = {}) }
        ShowcaseState("Log in") {
            MatuleLoginButton(text = "Войти с VK", onClick = {}, icon = { ServiceBadge("VK", Color(0xFF0077FF)) })
        }
        ShowcaseState("Log in") {
            MatuleLoginButton(text = "Войти с Yandex", onClick = {}, icon = { ServiceBadge("Я", Color(0xFFFFCC00)) })
        }
        ShowcaseState("Bubble · назад / фильтр / закрыть") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MatuleBubbleButton(icon = MatuleIcons.Back, onClick = {}, tint = MatuleTheme.colors.black)
                MatuleBubbleButton(icon = MatuleIcons.Filter, onClick = {})
                MatuleBubbleButton(
                    icon = MatuleIcons.Close,
                    onClick = {},
                    background = MatuleTheme.colors.accent,
                    tint = MatuleTheme.colors.white,
                )
            }
        }
        ShowcaseState("Текстовая кнопка") { MatuleTextButton(text = "Зарегистрироваться", onClick = {}) }
    }
}

/** Значок сервиса для кнопок входа (буквы на цветной плашке). */
@Composable
private fun ServiceBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

/** Переключатели и счётчики. */
@Composable
fun ControlsSection() {
    ShowcaseSection("Контролы") {
        var toggle by remember { mutableStateOf(true) }
        var count by remember { mutableIntStateOf(2) }
        ShowcaseState("Toggle · выключен / включён / неактивный") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MatuleToggle(checked = false, onCheckedChange = {})
                MatuleToggle(checked = true, onCheckedChange = {})
                MatuleToggle(checked = false, onCheckedChange = {}, enabled = false)
            }
        }
        ShowcaseState("Toggle · интерактивный") {
            MatuleToggle(checked = toggle, onCheckedChange = { toggle = it })
        }
        ShowcaseState("Counter · минимум / середина / максимум") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MatuleCounter(count = 1, onIncrement = {}, onDecrement = {})
                MatuleCounter(count = 5, onIncrement = {}, onDecrement = {})
                MatuleCounter(count = 99, onIncrement = {}, onDecrement = {})
            }
        }
        ShowcaseState("Counter · интерактивный: $count") {
            MatuleCounter(count = count, onIncrement = { count++ }, onDecrement = { count-- })
        }
    }
}
