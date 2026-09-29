package ru.matule.app.presentation.main.document

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.matule.uikit.components.header.MatuleHeader
import ru.matule.uikit.theme.MatuleTheme

/**
 * Каркас экранов с документами: хэдер из UIKit с кнопкой «назад» и содержимое под ним.
 *
 * @param title заголовок документа.
 * @param onBack «назад».
 * @param content содержимое.
 */
@Composable
fun DocumentScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MatuleTheme.colors.white)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        MatuleHeader(
            title = title,
            onBack = onBack,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 16.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            content = content,
        )
    }
}
