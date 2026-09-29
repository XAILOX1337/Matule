package ru.matule.app.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.matule.app.domain.model.Category
import ru.matule.app.domain.model.HomeData
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.model.Promotion
import java.io.File

/** Акция в кэше. */
@Serializable
private data class CachedPromotion(val id: String, val title: String, val price: Int, val colorHex: String, val imageUrl: String?)

/** Категория в кэше. */
@Serializable
private data class CachedCategory(val id: String, val title: String)

/** Описание в кэше. */
@Serializable
private data class CachedProduct(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val type: String,
    val consumption: String,
    val categoryId: String,
)

/** Главная страница в кэше. */
@Serializable
private data class CachedHome(
    val promotions: List<CachedPromotion>,
    val categories: List<CachedCategory>,
    val products: List<CachedProduct>,
)

/**
 * Кэш главной страницы в JSON-файле. Картинки акций кэширует Coil на диске отдельно.
 *
 * @param context контекст приложения.
 */
class HomeCacheDataSource(context: Context) {

    private val file = File(context.applicationContext.filesDir, FILE_NAME)
    private val json = Json { ignoreUnknownKeys = true }

    /** Сохранённая главная или `null`. */
    suspend fun read(): HomeData? = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext null
        runCatching { json.decodeFromString(CachedHome.serializer(), file.readText()).toDomain() }.getOrNull()
    }

    /** Сохраняет главную (через временный файл, чтобы не оставить битый кэш). */
    suspend fun write(home: HomeData) = withContext(Dispatchers.IO) {
        val temp = File(file.parentFile, "$FILE_NAME.tmp")
        temp.writeText(json.encodeToString(CachedHome.serializer(), home.toCached()))
        if (!temp.renameTo(file)) {
            file.delete()
            temp.renameTo(file)
        }
        Unit
    }

    private companion object {
        const val FILE_NAME = "home_cache.json"
    }
}

private fun CachedHome.toDomain() = HomeData(
    promotions = promotions.map { Promotion(it.id, it.title, it.price, it.colorHex, it.imageUrl) },
    categories = categories.map { Category(it.id, it.title) },
    products = products.map { Product(it.id, it.title, it.description, it.price, it.type, it.consumption, it.categoryId) },
)

private fun HomeData.toCached() = CachedHome(
    promotions = promotions.map { CachedPromotion(it.id, it.title, it.price, it.colorHex, it.imageUrl) },
    categories = categories.map { CachedCategory(it.id, it.title) },
    products = products.map { CachedProduct(it.id, it.title, it.description, it.price, it.type, it.consumption, it.categoryId) },
)
