package ru.matule.uikit.components.select

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.bottomsheet.MatuleBottomSheet
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleSizes
import ru.matule.uikit.theme.MatuleTheme

/**
 * Вариант выбора в селекте.
 *
 * @property id идентификатор варианта (что сохранять в модели).
 * @property title текст варианта.
 * @property emoji смайлик слева от текста; `null` — без смайлика.
 */
@Immutable
data class MatuleSelectOption(
    val id: String,
    val title: String,
    val emoji: String? = null,
)

/**
 * Селект (Селекты): выглядит как поле ввода со стрелкой, по нажатию открывает [MatuleBottomSheet]
 * со списком вариантов. Поддерживает смайлики у вариантов.
 *
 * @param options варианты выбора.
 * @param selected выбранный вариант; `null` — показывается плейсхолдер.
 * @param onSelect вызывается с выбранным вариантом, после чего окно закрывается.
 * @param modifier модификатор селекта.
 * @param placeholder текст, пока ничего не выбрано.
 * @param label подпись над селектом; `null` — без подписи.
 * @param sheetTitle заголовок окна выбора; по умолчанию — подпись или плейсхолдер.
 * @param error текст ошибки под селектом; не `null` — селект в состоянии ошибки.
 * @param enabled можно ли открыть выбор.
 */
@Composable
fun MatuleSelect(
    options: List<MatuleSelectOption>,
    selected: MatuleSelectOption?,
    onSelect: (MatuleSelectOption) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    sheetTitle: String = label ?: placeholder,
    error: String? = null,
    enabled: Boolean = true,
) {
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography
    val shape = MatuleTheme.shapes.medium
    var sheetOpen by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label != null) {
            Text(text = label, style = typography.captionRegular, color = colors.description)
            Spacer(Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MatuleSizes.inputHeight)
                .clip(shape)
                .background(if (error != null) colors.errorBackground else colors.inputBackground)
                .border(1.dp, if (error != null) colors.error else colors.inputStroke, shape)
                .clickable(enabled = enabled, role = Role.Button) { sheetOpen = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val emoji = selected?.emoji
            if (emoji != null) {
                Text(text = emoji, style = typography.textRegular)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = selected?.title ?: placeholder,
                style = typography.textRegular,
                color = if (selected == null || !enabled) colors.placeholder else colors.black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            MatuleIcon(icon = MatuleIcons.ChevronDown, size = 20.dp, tint = colors.inputIcon)
        }
        if (!error.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(text = error, style = typography.captionRegular, color = colors.error)
        }
    }

    if (sheetOpen) {
        MatuleBottomSheet(title = sheetTitle, onDismiss = { sheetOpen = false }) {
            options.forEach { option ->
                MatuleSelectRow(
                    option = option,
                    selected = option.id == selected?.id,
                    onClick = {
                        onSelect(option)
                        dismiss()
                    },
                )
            }
        }
    }
}

/**
 * Строка варианта в окне выбора: смайлик, текст и галочка у выбранного.
 *
 * @param option вариант.
 * @param selected выбран ли вариант сейчас.
 * @param onClick обработчик нажатия.
 * @param modifier модификатор строки.
 */
@Composable
fun MatuleSelectRow(
    option: MatuleSelectOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MatuleTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MatuleSizes.inputHeight)
            .clip(MatuleTheme.shapes.medium)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (option.emoji != null) {
            Text(text = option.emoji, style = MatuleTheme.typography.title3Regular)
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = option.title,
            style = MatuleTheme.typography.headlineRegular,
            color = colors.black,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            MatuleIcon(icon = MatuleIcons.Check, size = 20.dp, tint = colors.accent)
        }
    }
}
