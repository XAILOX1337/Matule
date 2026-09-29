package ru.matule.app.presentation.auth.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.matule.app.di.matuleViewModel
import ru.matule.app.presentation.common.ScreenTitle
import ru.matule.app.presentation.common.ScrollableScreen
import ru.matule.uikit.components.button.MatuleButton
import ru.matule.uikit.components.input.MatuleInput
import ru.matule.uikit.components.select.MatuleSelect
import ru.matule.uikit.components.select.MatuleSelectOption
import ru.matule.uikit.theme.MatuleTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Варианты пола: смайлики видны в списке BottomSheet. */
private val GenderOptions = listOf(
    MatuleSelectOption(id = "male", title = "Мужской", emoji = "👨"),
    MatuleSelectOption(id = "female", title = "Женский", emoji = "👩"),
)

/**
 * Экран «Создание профиля».
 *
 * @param onNext профиль заполнен — переход к созданию пароля.
 */
@Composable
fun CreateProfileScreen(onNext: () -> Unit) {
    val viewModel = matuleViewModel {
        CreateProfileViewModel(getProfileDraft, saveProfileDraft, submitProfileDraft, validateEmail, notifier)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnNext by rememberUpdatedState(onNext)
    LaunchedEffect(viewModel) {
        viewModel.next.collect { currentOnNext() }
    }
    CreateProfileContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onPatronymicChange = viewModel::onPatronymicChange,
        onSurnameChange = viewModel::onSurnameChange,
        onBirthdayPicked = viewModel::onBirthdayPicked,
        onGenderChange = viewModel::onGenderChange,
        onEmailChange = viewModel::onEmailChange,
        onSubmit = viewModel::submit,
    )
}

/**
 * Вёрстка экрана «Создание профиля».
 *
 * @param state состояние.
 * @param onNameChange ввод имени.
 * @param onPatronymicChange ввод отчества.
 * @param onSurnameChange ввод фамилии.
 * @param onBirthdayPicked выбор даты рождения.
 * @param onGenderChange выбор пола.
 * @param onEmailChange ввод почты.
 * @param onSubmit «Далее».
 */
@Composable
fun CreateProfileContent(
    state: CreateProfileUiState,
    onNameChange: (String) -> Unit,
    onPatronymicChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onBirthdayPicked: (LocalDate) -> Unit,
    onGenderChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    val focusManager = LocalFocusManager.current
    var pickerOpen by rememberSaveable { mutableStateOf(false) }
    val nameOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    ScrollableScreen {
        Spacer(Modifier.height(32.dp))
        ScreenTitle(title = "Создание Профиля")
        Spacer(Modifier.height(44.dp))
        Text(
            text = "Без профиля вы не сможете создавать проекты.",
            style = typography.captionRegular,
            color = colors.description,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "В профиле будут храниться результаты проектов и ваши описания.",
            style = typography.captionRegular,
            color = colors.description,
        )
        Spacer(Modifier.height(32.dp))
        MatuleInput(
            value = state.name,
            onValueChange = onNameChange,
            placeholder = "Имя",
            keyboardOptions = nameOptions,
            keyboardActions = next,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        MatuleInput(
            value = state.patronymic,
            onValueChange = onPatronymicChange,
            placeholder = "Отчество",
            keyboardOptions = nameOptions,
            keyboardActions = next,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        MatuleInput(
            value = state.surname,
            onValueChange = onSurnameChange,
            placeholder = "Фамилия",
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        // Дата выбирается в календаре и показывается как в макете: «28 февраля 1991».
        Box(modifier = Modifier.fillMaxWidth()) {
            MatuleInput(
                value = state.birthdayText,
                onValueChange = {},
                placeholder = "Дата рождения",
                readOnly = true,
                error = state.birthdayError,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(role = Role.Button) {
                        focusManager.clearFocus()
                        pickerOpen = true
                    },
            )
        }
        Spacer(Modifier.height(24.dp))
        val selectedGender = GenderOptions.firstOrNull { it.title == state.gender }
        MatuleSelect(
            options = GenderOptions,
            // В поле — только текст, как в макете; смайлики — в списке вариантов.
            selected = selectedGender?.copy(emoji = null),
            onSelect = { option -> onGenderChange(option.title) },
            placeholder = "Пол",
            sheetTitle = "Пол",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        MatuleInput(
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "Почта",
            error = state.emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onSubmit()
            }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        MatuleButton(
            text = "Далее",
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            enabled = state.isFilled,
            loading = state.isLoading,
        )
        Spacer(Modifier.height(20.dp))
    }

    if (pickerOpen) {
        BirthdayPickerDialog(
            initial = state.birthdayDate,
            onPicked = onBirthdayPicked,
            onDismiss = { pickerOpen = false },
        )
    }
}

/** Разрешены только прошедшие даты. */
@OptIn(ExperimentalMaterial3Api::class)
private object PastDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= System.currentTimeMillis()

    override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now().year
}

/**
 * Календарь выбора даты рождения.
 *
 * @param initial выбранная ранее дата.
 * @param onPicked дата выбрана.
 * @param onDismiss закрыть календарь.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayPickerDialog(
    initial: LocalDate?,
    onPicked: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = remember(initial) { initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        yearRange = 1900..LocalDate.now().year,
        selectableDates = PastDates,
    )
    val colors = MatuleTheme.colors
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onPicked(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    onDismiss()
                },
                enabled = pickerState.selectedDateMillis != null,
            ) {
                Text(text = "Готово", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Отмена", color = colors.accent)
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}
