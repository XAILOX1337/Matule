package ru.matule.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.matule.app.domain.model.ImageFile
import java.io.File
import kotlin.math.max

/**
 * Фото проектов в файлах приложения. Картинка из галереи или камеры уменьшается
 * до [MAX_SIDE] пикселей по большей стороне и сохраняется в JPEG — так она быстро уходит на сервер.
 *
 * @param context контекст приложения.
 */
class ProjectImageStorage(context: Context) {

    private val appContext = context.applicationContext
    private val directory = File(appContext.filesDir, DIRECTORY)

    /**
     * Сохраняет картинку.
     *
     * @param sourceUri адрес картинки.
     * @return путь к файлу.
     */
    suspend fun import(sourceUri: String): String = withContext(Dispatchers.IO) {
        val source = ImageDecoder.createSource(appContext.contentResolver, Uri.parse(sourceUri))
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val largest = max(info.size.width, info.size.height)
            if (largest > MAX_SIDE) {
                val scale = MAX_SIDE.toFloat() / largest
                decoder.setTargetSize((info.size.width * scale).toInt(), (info.size.height * scale).toInt())
            }
            // Программный буфер: аппаратный Bitmap нельзя сжать в JPEG.
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        directory.mkdirs()
        // Старые черновые фото больше не нужны: в черновике всегда одно фото.
        directory.listFiles()?.forEach { it.delete() }
        val file = File(directory, "project_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        bitmap.recycle()
        file.absolutePath
    }

    /** Файл для отправки или `null`, если его нет. */
    suspend fun load(path: String): ImageFile? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) return@withContext null
        ImageFile(fileName = file.name, mimeType = "image/jpeg", bytes = file.readBytes())
    }

    private companion object {
        const val DIRECTORY = "project_images"
        const val MAX_SIDE = 1600
        const val JPEG_QUALITY = 85
    }
}
