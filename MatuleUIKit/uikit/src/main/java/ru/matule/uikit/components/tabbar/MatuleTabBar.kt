package ru.matule.uikit.components.tabbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.divider.MatuleDivider
import ru.matule.uikit.icons.MatuleIcon
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/**
 * Вкладка таббара.
 *
 * @property title подпись под иконкой.
 * @property icon иконка вкладки.
 */
@Immutable
data class MatuleTabItem(
    val title: String,
    val icon: MatuleIcons,
)

/**
 * Нижняя панель вкладок (Tabbar): иконка и подпись, активная вкладка — синего цвета.
 * Сама добавляет отступ под системную навигацию.
 *
 * @param items вкладки слева направо.
 * @param selectedIndex индекс активной вкладки.
 * @param onSelect вызывается с индексом нажатой вкладки.
 * @param modifier модификатор панели.
 */
@Composable
fun MatuleTabBar(
    items: List<MatuleTabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MatuleTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.white),
    ) {
        MatuleDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            items.forEachIndexed { index, item ->
                val color = if (index == selectedIndex) colors.accent else colors.inputIcon
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                        ) { onSelect(index) }
                        .padding(top = 8.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MatuleIcon(icon = item.icon, size = 28.dp, tint = color)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.title,
                        style = MatuleTheme.typography.caption2Regular,
                        color = color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
