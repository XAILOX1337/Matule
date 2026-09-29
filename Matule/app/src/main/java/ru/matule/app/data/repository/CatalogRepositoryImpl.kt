package ru.matule.app.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.matule.app.data.local.HomeCacheDataSource
import ru.matule.app.data.remote.CatalogRemoteDataSource
import ru.matule.app.domain.model.Category
import ru.matule.app.domain.model.HomeData
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.repository.CatalogRepository
import ru.matule.app.domain.result.AppResult

/**
 * Каталог: сервер + кэш главной страницы.
 *
 * @param remote запросы каталога.
 * @param homeCache кэш главной.
 */
class CatalogRepositoryImpl(
    private val remote: CatalogRemoteDataSource,
    private val homeCache: HomeCacheDataSource,
) : CatalogRepository {

    override suspend fun cachedHome(): HomeData? = homeCache.read()

    override suspend fun loadHome(): AppResult<HomeData> = coroutineScope {
        // Три запроса главной идут параллельно.
        val promotions = async { remote.promotions() }
        val categories = async { remote.categories() }
        val products = async { remote.products(null) }
        val promotionsResult = promotions.await()
        val categoriesResult = categories.await()
        val productsResult = products.await()
        when {
            promotionsResult is AppResult.Failure -> promotionsResult
            categoriesResult is AppResult.Failure -> categoriesResult
            productsResult is AppResult.Failure -> productsResult
            else -> {
                val home = HomeData(
                    promotions = (promotionsResult as AppResult.Success).data,
                    categories = (categoriesResult as AppResult.Success).data,
                    products = (productsResult as AppResult.Success).data,
                )
                homeCache.write(home)
                AppResult.Success(home)
            }
        }
    }

    override suspend fun categories(): AppResult<List<Category>> {
        val result = remote.categories()
        if (result is AppResult.Success) return result
        val cached = homeCache.read()?.categories
        return if (cached.isNullOrEmpty()) result else AppResult.Success(cached)
    }

    override suspend fun products(categoryId: String?): AppResult<List<Product>> = remote.products(categoryId)

    override suspend fun search(query: String, categoryId: String?): AppResult<List<Product>> =
        remote.search(query, categoryId)

    override suspend fun product(productId: String): AppResult<Product> = remote.product(productId)
}
