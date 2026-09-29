package ru.matule.uikit.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleSizes
import ru.matule.uikit.theme.MatuleTheme

/**
 * Поле ввода (Инпуты) со всеми состояниями из макета: пустое с плейсхолдером, заполненное,
 * с подписью сверху, в фокусе (синяя обводка), с ошибкой (красная обводка и текст под полем),
 * неактивное и поле пароля с кнопкой «глаз».
 *
 * @param value текущий текст.
 * @param onValueChange вызывается при вводе текста.
 * @param modifier модификатор поля.
 * @param label подпись над полем; `null` — без подписи.
 * @param placeholder серый текст-подсказка в пустом поле.
 * @param error текст ошибки под полем; не `null` — поле в состоянии ошибки.
 * @param enabled можно ли редактировать и фокусировать поле.
 * @param readOnly только чтение: поле фокусируется, но текст не меняется.
 * @param isPassword поле пароля: символы скрыты, справа кнопка «глаз» для показа.
 * @param initiallyPasswordVisible показать ли пароль сразу (для поля пароля).
 * @param keyboardOptions тип клавиатуры и кнопка действия.
 * @param keyboardActions обработчики кнопки действия клавиатуры.
 * @param visualTransformation преобразование отображаемого текста, например маска даты.
 * @param leadingContent содержимое слева от текста (иконка, смайлик); `null` — нет.
 * @param trailingContent содержимое справа от текста; не используется в поле пароля.
 * @param interactionSource источник событий фокуса и нажатий; `null` — создаётся внутри.
 */
@Composable
fun MatuleInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    error: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isPassword: Boolean = false,
    initiallyPasswordVisible: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    val shape = MatuleTheme.shapes.medium
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    var passwordVisible by rememberSaveable { mutableStateOf(initiallyPasswordVisible) }
    val isError = error != null

    val borderColor = when {
        isError -> colors.error
        focused && enabled -> colors.accent
        else -> colors.inputStroke
    }
    val background = if (isError) colors.errorBackground else colors.inputBackground
    val transformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else visualTransformation
    val options = if (isPassword) {
        KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = keyboardOptions.imeAction)
    } else {
        keyboardOptions
    }

    Column(modifier = modifier) {
        if (label != null) {
            Text(text = label, style = typography.captionRegular, color = colors.description)
            Spacer(Modifier.height(8.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = typography.textRegular.copy(color = if (enabled) colors.black else colors.placeholder),
            keyboardOptions = options,
            keyboardActions = keyboardActions,
            singleLine = true,
            visualTransformation = transformation,
            interactionSource = source,
            cursorBrush = SolidColor(colors.accent),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MatuleSizes.inputHeight)
                        .clip(shape)
                        .background(background)
                        .border(1.dp, borderColor, shape)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingContent != null) {
                        leadingContent()
                        Spacer(Modifier.width(8.dp))
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = typography.textRegular,
                                color = colors.placeholder,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                    if (isPassword) {
                        Spacer(Modifier.width(8.dp))
                        MatuleIcon(
                            icon = if (passwordVisible) MatuleIcons.Eye else MatuleIcons.EyeOff,
                            size = 22.dp,
                            tint = colors.black,
                            contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                            modifier = Modifier
                                .clip(MatuleTheme.shapes.circle)
                                .clickable { passwordVisible = !passwordVisible },
                        )
                    } else if (trailingContent != null) {
                        Spacer(Modifier.width(8.dp))
                        trailingContent()
                    }
                }
            },
        )
        if (!error.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(text = error, style = typography.captionRegular, color = colors.error)
        }
    }
}
