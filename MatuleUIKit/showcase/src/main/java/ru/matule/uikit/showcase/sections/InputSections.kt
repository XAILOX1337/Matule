package ru.matule.uikit.showcase.sections

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.bottomsheet.MatuleBottomSheet
import ru.matule.uikit.components.button.MatuleButton
import ru.matule.uikit.components.button.MatuleButtonStyle
import ru.matule.uikit.components.input.MatuleDateInput
import ru.matule.uikit.components.input.MatuleInput
import ru.matule.uikit.components.search.MatuleSearch
import ru.matule.uikit.components.search.MatuleSearchButton
import ru.matule.uikit.components.select.MatuleSelect
import ru.matule.uikit.components.select.MatuleSelectOption
import ru.matule.uikit.showcase.ShowcaseSection
import ru.matule.uikit.showcase.ShowcaseState
import ru.matule.uikit.theme.MatuleTheme

/** Поля ввода во всех состояниях. */
@Composable
fun InputsSection() {
    ShowcaseSection("Инпуты") {
        var empty by remember { mutableStateOf("") }
        var filled by remember { mutableStateOf("Иван") }
        var labeled by remember { mutableStateOf("") }
        var focused by remember { mutableStateOf("") }
        var withError by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("oNQZnz\$Hx2") }
        var passwordShown by remember { mutableStateOf("1324534789") }
        var date by remember { mutableStateOf("") }
        val focusSource = remember { MutableInteractionSource() }
        LaunchedEffect(focusSource) { focusSource.emit(FocusInteraction.Focus()) }

        ShowcaseState("Пустое, с плейсхолдером") {
            MatuleInput(value = empty, onValueChange = { empty = it }, placeholder = "Введите имя")
        }
        ShowcaseState("Заполненное") {
            MatuleInput(value = filled, onValueChange = { filled = it }, placeholder = "Введите имя")
        }
        ShowcaseState("С подписью") {
            MatuleInput(value = labeled, onValueChange = { labeled = it }, label = "Имя", placeholder = "Введите имя")
        }
        ShowcaseState("В фокусе") {
            MatuleInput(
                value = focused,
                onValueChange = { focused = it },
                label = "Имя",
                placeholder = "Введите имя",
                interactionSource = focusSource,
            )
        }
        ShowcaseState("Ошибка") {
            MatuleInput(
                value = withError,
                onValueChange = { withError = it },
                label = "Имя",
                placeholder = "Имя",
                error = "Введите ваше имя",
            )
        }
        ShowcaseState("Неактивное") {
            MatuleInput(value = "Эдуард", onValueChange = {}, label = "Имя", enabled = false)
        }
        ShowcaseState("Пароль скрыт (нажмите на глаз)") {
            MatuleInput(value = password, onValueChange = { password = it }, label = "Пароль", isPassword = true)
        }
        ShowcaseState("Пароль показан") {
            MatuleInput(
                value = passwordShown,
                onValueChange = { passwordShown = it },
                label = "Пароль",
                isPassword = true,
                initiallyPasswordVisible = true,
            )
        }
        ShowcaseState("Дата с маской дд.мм.гггг") {
            MatuleDateInput(digits = date, onDigitsChange = { date = it }, label = "Дата рождения")
        }
    }
}

private val genderOptions = listOf(
    MatuleSelectOption(id = "male", title = "Мужской", emoji = "👨"),
    MatuleSelectOption(id = "female", title = "Женский", emoji = "👩"),
)

private val recipientOptions = listOf(
    MatuleSelectOption(id = "self", title = "Себе", emoji = "🙋"),
    MatuleSelectOption(id = "kid", title = "Ребёнку", emoji = "👶"),
    MatuleSelectOption(id = "friend", title = "Гарвард Троцкий", emoji = "🧑"),
)

/** Селекты во всех состояниях; по нажатию открывается BottomSheet со списком. */
@Composable
fun SelectsSection() {
    ShowcaseSection("Селекты") {
        var first by remember { mutableStateOf<MatuleSelectOption?>(null) }
        var second by remember { mutableStateOf<MatuleSelectOption?>(genderOptions.first()) }
        var third by remember { mutableStateOf<MatuleSelectOption?>(recipientOptions.last()) }
        var fourth by remember { mutableStateOf<MatuleSelectOption?>(null) }

        ShowcaseState("Ничего не выбрано") {
            MatuleSelect(options = genderOptions, selected = first, onSelect = { first = it }, placeholder = "Пол")
        }
        ShowcaseState("Выбрано значение") {
            MatuleSelect(options = genderOptions, selected = second, onSelect = { second = it }, placeholder = "Пол")
        }
        ShowcaseState("Со смайликом") {
            MatuleSelect(
                options = recipientOptions,
                selected = third,
                onSelect = { third = it },
                label = "Кому",
                placeholder = "Выберите кому",
            )
        }
        ShowcaseState("Ошибка") {
            MatuleSelect(
                options = genderOptions,
                selected = fourth,
                onSelect = { fourth = it },
                placeholder = "Пол",
                error = "Выберите пол",
            )
        }
        ShowcaseState("Неактивный") {
            MatuleSelect(options = genderOptions, selected = null, onSelect = {}, placeholder = "Пол", enabled = false)
        }
    }
}

/** BottomSheet: высота по контенту или весь экран со скроллом. */
@Composable
fun ModalSection() {
    ShowcaseSection("Модальные окна") {
        var shortOpen by remember { mutableStateOf(false) }
        var longOpen by remember { mutableStateOf(false) }
        ShowcaseState("Высота по контенту") {
            MatuleButton(text = "Открыть короткое окно", onClick = { shortOpen = true }, style = MatuleButtonStyle.Secondary)
        }
        ShowcaseState("Контент не помещается — весь экран и скролл") {
            MatuleButton(text = "Открыть длинное окно", onClick = { longOpen = true }, style = MatuleButtonStyle.Secondary)
        }
        if (shortOpen) {
            MatuleBottomSheet(title = "Рубашка Воскресенье для машинного вязания", onDismiss = { shortOpen = false }) {
                Text(text = "Описание", style = MatuleTheme.typography.captionRegular, color = MatuleTheme.colors.placeholder)
                Spacer(Modifier.height(8.dp))
                Text(text = "Короткое описание товара.", style = MatuleTheme.typography.textRegular)
                Spacer(Modifier.height(24.dp))
                MatuleButton(text = "Добавить за 690 ₽", onClick = { dismiss() })
            }
        }
        if (longOpen) {
            MatuleBottomSheet(title = "Длинный контент", onDismiss = { longOpen = false }) {
                repeat(40) { index ->
                    Text(
                        text = "Строка ${index + 1}: окно раскрыто на весь экран, содержимое прокручивается.",
                        style = MatuleTheme.typography.textRegular,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                MatuleButton(text = "Закрыть", onClick = { dismiss() })
            }
        }
    }
}

/** Поле поиска. */
@Composable
fun SearchSection() {
    ShowcaseSection("Поиск") {
        var empty by remember { mutableStateOf("") }
        var filled by remember { mutableStateOf("Рубашка") }
        ShowcaseState("Пустое") {
            MatuleSearch(query = empty, onQueryChange = { empty = it }, placeholder = "Искать описание")
        }
        ShowcaseState("С текстом и кнопкой очистки") {
            MatuleSearch(query = filled, onQueryChange = { filled = it }, placeholder = "Искать описание")
        }
        ShowcaseState("Кнопка-поиск (открывает экран поиска)") {
            MatuleSearchButton(onClick = {})
        }
    }
}
