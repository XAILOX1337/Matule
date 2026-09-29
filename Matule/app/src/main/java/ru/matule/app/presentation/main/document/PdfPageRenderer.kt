package ru.matule.app.presentation.main.document

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

/**
 * Отрисовка PDF из assets системным PdfRenderer — без сторонних библиотек.
 */
object PdfPageRenderer {

    /**
     * Рисует все страницы документа.
     * Вызывать не в главном потоке.
     *
     * @param context контекст.
     * @param assetName имя файла в assets.
     * @param pageWidthPx ширина страницы в пикселях.
     * @return страницы документа.
     */
    fun render(context: Context, assetName: String, pageWidthPx: Int): List<ImageBitmap> {
        // PdfRenderer читает только файл с произвольным доступом, поэтому копируем PDF из assets в кэш.
        val file = File(context.cacheDir, assetName)
        context.assets.open(assetName).use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                (0 until renderer.pageCount).map { index ->
                    renderer.openPage(index).use { page ->
                        val height = (page.height.toFloat() * pageWidthPx / page.width).toInt()
                        val bitmap = Bitmap.createBitmap(pageWidthPx, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap.asImageBitmap()
                    }
                }
            }
        }
    }
}
