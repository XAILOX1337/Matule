package ru.matule.app.presentation.main.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.matule.uikit.theme.MatuleTheme

/** PDF политики конфиденциальности в assets. */
private const val PRIVACY_POLICY_ASSET = "privacy_policy.pdf"

/**
 * Экран «Политика конфиденциальности»: PDF внутри приложения.
 *
 * @param onBack «назад».
 */
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    DocumentScaffold(title = "Политика конфиденциальности", onBack = onBack) {
        PdfDocument(assetName = PRIVACY_POLICY_ASSET)
    }
}

/**
 * Просмотр PDF: страницы друг под другом с прокруткой.
 *
 * @param assetName имя файла в assets.
 * @param modifier модификатор.
 */
@Composable
fun PdfDocument(assetName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = MatuleTheme.colors
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val pageWidthPx = with(LocalDensity.current) { (maxWidth - 40.dp).roundToPx() }.coerceAtLeast(1)
        val pages by produceState<List<ImageBitmap>?>(initialValue = null, assetName, pageWidthPx) {
            value = withContext(Dispatchers.IO) {
                runCatching { PdfPageRenderer.render(context, assetName, pageWidthPx) }.getOrDefault(emptyList())
            }
        }
        val loaded = pages
        when {
            loaded == null -> CircularProgressIndicator(
                color = colors.accent,
                modifier = Modifier.align(Alignment.Center),
            )
            loaded.isEmpty() -> Text(
                text = "Не удалось открыть документ",
                style = MatuleTheme.typography.textRegular,
                color = colors.description,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(loaded) { index, page ->
                    Image(
                        bitmap = page,
                        contentDescription = "Страница ${index + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(page.width.toFloat() / page.height)
                            .border(1.dp, colors.cardStroke, MatuleTheme.shapes.small),
                    )
                }
            }
        }
    }
}
