package ru.matule.app.presentation.main.projects.create

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Файлы для снимков камеры: камера пишет фото по адресу FileProvider в кэш приложения.
 */
object CameraFiles {

    /**
     * Новый адрес для снимка.
     *
     * @param context контекст.
     * @return content:// адрес файла.
     */
    fun newPhotoUri(context: Context): Uri {
        val directory = File(context.cacheDir, DIRECTORY).apply { mkdirs() }
        val file = File(directory, "photo_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}$AUTHORITY_SUFFIX", file)
    }

    /** Папка в кэше (см. res/xml/file_paths.xml). */
    private const val DIRECTORY = "camera"

    /** Суффикс authority FileProvider из манифеста. */
    private const val AUTHORITY_SUFFIX = ".fileprovider"
}
