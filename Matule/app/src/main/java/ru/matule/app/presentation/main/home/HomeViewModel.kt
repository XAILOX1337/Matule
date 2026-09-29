package ru.matule.app.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.matule.app.domain.model.Category
import ru.matule.app.domain.model.HomeData
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.model.Promotion
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.GetCachedHomeUseCase
import ru.matule.app.domain.usecase.IsNotificationsEnabledUseCase
import ru.matule.app.domain.usecase.LoadHomeUseCase
import ru.matule.app.presentation.common.Notifier
import ru.matule.app.presentation.main.common.CartController

/**
 * Состояние экрана «Главная».
 *
 * @property promotions акции и новости.
 * @property categories категории.
 * @property selectedCategoryId выбранная категория; `null` — все.
 * @property query поисковый запрос.
 * @property products описания с учётом категории и поиска.
 * @property cartProductIds товары в корзине.
 * @property pendingProductIds товары, по которым идёт запрос.
 * @property isLoading первая загрузка (кэша ещё нет).
 */
data class HomeUiState(
    val promotions: List<Promotion> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val query: String = "",
    val products: List<Product> = emptyList(),
    val cartProductIds: Set<String> = emptySet(),
    val pendingProductIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
)

/** Фильтр главной. */
private data class HomeFilter(val query: String = "", val categoryId: String? = null)

/**
 * Экран «Главная»: сначала кэш (работает без сети), затем свежие данные с сервера.
 * Категории и поиск фильтруют загруженные описания сразу, без запросов.
 *
 * @param getCachedHome главная из кэша.
 * @param loadHome главная с сервера.
 * @param isNotificationsEnabled включены ли уведомления в профиле.
 * @param notifier уведомления об ошибках.
 * @param createCartController кнопки корзины в области корутин этой ViewModel.
 */
class HomeViewModel(
    private val getCachedHome: GetCachedHomeUseCase,
    private val loadHome: LoadHomeUseCase,
    private val isNotificationsEnabled: IsNotificationsEnabledUseCase,
    private val notifier: Notifier,
    createCartController: (CoroutineScope) -> CartController,
) : ViewModel() {

    /** Кнопки «Добавить» и «Убрать». */
    val cart: CartController = createCartController(viewModelScope)

    private val notifications = MutableStateFlow(false)

    /** Уведомления включены в профиле — можно попросить разрешение Android на их показ. */
    val notificationsEnabled: StateFlow<Boolean> = notifications.asStateFlow()

    private val home = MutableStateFlow<HomeData?>(null)
    private val filter = MutableStateFlow(HomeFilter())
    private val loading = MutableStateFlow(true)

    /** Состояние экрана. */
    val state: StateFlow<HomeUiState> = combine(home, filter, cart.items, cart.pending, loading) { data, current, items, pending, isLoading ->
        HomeUiState(
            promotions = data?.promotions.orEmpty(),
            categories = data?.categories.orEmpty(),
            selectedCategoryId = current.categoryId,
            query = current.query,
            products = data?.products.orEmpty().filter { it.matches(current) },
            cartProductIds = items.map { it.product.id }.toSet(),
            pendingProductIds = pending,
            isLoading = isLoading && data == null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState(isLoading = true))

    init {
        refresh()
        cart.load()
        viewModelScope.launch { notifications.value = isNotificationsEnabled() }
    }

    /** Загрузить главную заново. */
    fun refresh() {
        viewModelScope.launch {
            if (home.value == null) home.value = getCachedHome()
            loading.value = true
            when (val result = loadHome()) {
                is AppResult.Success -> home.value = result.data
                is AppResult.Failure -> notifier.showError(result.error)
            }
            loading.value = false
        }
    }

    /** Ввод поиска. */
    fun onQueryChange(query: String) = filter.update { it.copy(query = query) }

    /** Выбор категории. */
    fun onCategorySelected(categoryId: String?) = filter.update { it.copy(categoryId = categoryId) }

    private fun Product.matches(filter: HomeFilter): Boolean {
        if (filter.categoryId != null && categoryId != filter.categoryId) return false
        val text = filter.query.trim()
        return text.isEmpty() || title.contains(text, ignoreCase = true) || description.contains(text, ignoreCase = true)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
