package ru.matule.app.presentation.main.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.matule.app.domain.model.Category
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.GetCachedHomeUseCase
import ru.matule.app.domain.usecase.GetCatalogFilterUseCase
import ru.matule.app.domain.usecase.GetCatalogUseCase
import ru.matule.app.domain.usecase.GetCategoriesUseCase
import ru.matule.app.domain.usecase.SaveCatalogFilterUseCase
import ru.matule.app.presentation.common.Notifier
import ru.matule.app.presentation.main.common.CartController

/**
 * Состояние экрана «Каталог».
 *
 * @property categories категории.
 * @property selectedCategoryId выбранная категория; `null` — все.
 * @property query поисковый запрос.
 * @property products описания.
 * @property cartProductIds товары в корзине.
 * @property pendingProductIds товары, по которым идёт запрос.
 * @property cartTotal сумма корзины (для кнопки «В корзину»).
 * @property isLoading идёт загрузка.
 */
data class CatalogUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val query: String = "",
    val products: List<Product> = emptyList(),
    val cartProductIds: Set<String> = emptySet(),
    val pendingProductIds: Set<String> = emptySet(),
    val cartTotal: Int = 0,
    val isLoading: Boolean = false,
)

/** Фильтр каталога. */
private data class CatalogFilter(val query: String = "", val categoryId: String? = null)

/** Список каталога. */
private data class CatalogList(val products: List<Product> = emptyList(), val isLoading: Boolean = true)

/**
 * Экран «Каталог»: поиск и категории выполняются на сервере (с задержкой ввода),
 * фильтр запоминается между запусками.
 *
 * @param getCategories категории.
 * @param getCachedHome главная из кэша — описания без сети.
 * @param getCatalog список или поиск.
 * @param getCatalogFilter сохранённый фильтр.
 * @param saveCatalogFilter сохранение фильтра.
 * @param notifier уведомления об ошибках.
 * @param createCartController кнопки корзины в области корутин этой ViewModel.
 */
@OptIn(FlowPreview::class)
class CatalogViewModel(
    private val getCategories: GetCategoriesUseCase,
    private val getCachedHome: GetCachedHomeUseCase,
    private val getCatalog: GetCatalogUseCase,
    private val getCatalogFilter: GetCatalogFilterUseCase,
    private val saveCatalogFilter: SaveCatalogFilterUseCase,
    private val notifier: Notifier,
    createCartController: (CoroutineScope) -> CartController,
) : ViewModel() {

    /** Кнопки «Добавить» и «Убрать». */
    val cart: CartController = createCartController(viewModelScope)

    private val categories = MutableStateFlow<List<Category>>(emptyList())
    private val filter = MutableStateFlow(CatalogFilter())
    private val list = MutableStateFlow(CatalogList())

    /** Состояние экрана. */
    val state: StateFlow<CatalogUiState> =
        combine(categories, filter, list, cart.items, cart.pending) { allCategories, current, loaded, items, pending ->
            CatalogUiState(
                categories = allCategories,
                selectedCategoryId = current.categoryId,
                query = current.query,
                products = loaded.products,
                cartProductIds = items.map { it.product.id }.toSet(),
                pendingProductIds = pending,
                cartTotal = items.sumOf { it.total },
                isLoading = loaded.isLoading && loaded.products.isEmpty(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CatalogUiState(isLoading = true))

    init {
        cart.load()
        viewModelScope.launch {
            when (val result = getCategories()) {
                is AppResult.Success -> categories.value = result.data
                is AppResult.Failure -> notifier.showError(result.error)
            }
        }
        viewModelScope.launch {
            val (query, categoryId) = getCatalogFilter()
            filter.value = CatalogFilter(query, categoryId)
            filter.debounce(SEARCH_DELAY_MS).distinctUntilChanged().collectLatest { current ->
                list.update { it.copy(isLoading = true) }
                saveCatalogFilter(current.query, current.categoryId)
                when (val result = getCatalog(current.query, current.categoryId)) {
                    is AppResult.Success -> list.value = CatalogList(result.data, isLoading = false)
                    is AppResult.Failure -> {
                        notifier.showError(result.error)
                        list.value = CatalogList(offlineProducts(current), isLoading = false)
                    }
                }
            }
        }
    }

    /** Ввод поиска. */
    fun onQueryChange(query: String) = filter.update { it.copy(query = query) }

    /** Выбор категории. */
    fun onCategorySelected(categoryId: String?) = filter.update { it.copy(categoryId = categoryId) }

    /** Без сети показываем описания из кэша главной с тем же фильтром. */
    private suspend fun offlineProducts(current: CatalogFilter): List<Product> {
        val text = current.query.trim()
        return getCachedHome()?.products.orEmpty().filter { product ->
            (current.categoryId == null || product.categoryId == current.categoryId) &&
                (text.isEmpty() || product.title.contains(text, ignoreCase = true))
        }
    }

    private companion object {
        const val SEARCH_DELAY_MS = 300L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
