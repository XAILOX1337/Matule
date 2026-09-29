package ru.matule.uikit.components.input

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Поле даты с маской `дд.мм.гггг` (Инпуты → поле с маской «--.--.----»).
 * Хранит только цифры (до 8), точки подставляются при отображении.
 *
 * @param digits введённые цифры даты без точек, например «28021991».
 * @param onDigitsChange вызывается с новыми цифрами (лишние символы отбрасываются).
 * @param modifier модификатор поля.
 * @param label подпись над полем.
 * @param placeholder подсказка в пустом поле.
 * @param error текст ошибки под полем.
 * @param enabled можно ли редактировать поле.
 * @param imeAction кнопка действия клавиатуры.
 * @param keyboardActions обработчики кнопки действия клавиатуры.
 */
@Composable
fun MatuleDateInput(
    digits: String,
    onDigitsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "--.--.----",
    error: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val transformation = remember { DateVisualTransformation() }
    MatuleInput(
        value = digits,
        onValueChange = { text -> onDigitsChange(text.filter(Char::isDigit).take(DATE_DIGITS)) },
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        error = error,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = keyboardActions,
        visualTransformation = transformation,
    )
}

private const val DATE_DIGITS = 8

/**
 * Маска даты: вставляет точки после 2-й и 4-й цифры («28021991» → «28.02.1991»).
 * Позиция курсора пересчитывается с учётом вставленных точек.
 */
class DateVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(DATE_DIGITS)
        val formatted = formatDateDigits(digits)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val transformed = when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> offset + 2
                }
                return transformed.coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val original = when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> offset - 2
                }
                return original.coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

/**
 * Форматирует цифры даты с точками: «2802» → «28.02», «28021991» → «28.02.1991».
 *
 * @param digits цифры даты без разделителей.
 * @return строка с точками после дня и месяца.
 */
fun formatDateDigits(digits: String): String = buildString {
    digits.forEachIndexed { index, char ->
        append(char)
        if ((index == 1 || index == 3) && index != digits.lastIndex) append('.')
    }
}
