package ru.matule.app.presentation.main.document

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.matule.uikit.theme.MatuleTheme

/** Текст пользовательского соглашения в assets. */
private const val USER_AGREEMENT_ASSET = "user_agreement.txt"

/**
 * Экран «Пользовательское соглашение».
 *
 * @param onBack «назад».
 */
@Composable
fun UserAgreementScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val text by produceState<String?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            runCatching { context.assets.open(USER_AGREEMENT_ASSET).bufferedReader().use { it.readText() } }
                .getOrDefault("Не удалось открыть документ")
        }
    }
    DocumentScaffold(title = "Пользовательское соглашение", onBack = onBack) {
        val loaded = text
        if (loaded == null) {
            CircularProgressIndicator(color = MatuleTheme.colors.accent, modifier = Modifier.align(Alignment.Center))
        } else {
            Text(
                text = loaded,
                style = MatuleTheme.typography.textRegular,
                color = MatuleTheme.colors.black,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            )
        }
    }
}
