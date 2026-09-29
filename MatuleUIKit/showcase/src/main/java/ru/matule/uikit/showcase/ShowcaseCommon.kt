package ru.matule.uikit.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.divider.MatuleDivider
import ru.matule.uikit.theme.MatuleTheme

/**
 * Секция демо-экрана: крупный заголовок и компоненты под ним.
 *
 * @param title название секции (как в макете).
 * @param content содержимое секции.
 */
@Composable
fun ShowcaseSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)) {
        Text(text = title, style = MatuleTheme.typography.title1ExtraBold, color = MatuleTheme.colors.black)
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
    MatuleDivider()
}

/**
 * Подпись состояния компонента и сам компонент под ней.
 *
 * @param label название состояния.
 * @param content компонент в этом состоянии.
 */
@Composable
fun ShowcaseState(label: String, content: @Composable () -> Unit) {
    Column {
        Text(text = label, style = MatuleTheme.typography.caption2Regular, color = MatuleTheme.colors.placeholder)
        Spacer(Modifier.height(6.dp))
        content()
    }
}
