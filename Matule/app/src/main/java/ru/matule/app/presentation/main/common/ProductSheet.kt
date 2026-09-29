package ru.matule.app.presentation.main.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.matule.app.di.matuleViewModel
import ru.matule.app.domain.model.Product
import ru.matule.app.domain.result.AppResult
import ru.matule.app.domain.usecase.GetProductUseCase
import ru.matule.app.presentation.common.Formatters
import ru.matule.app.presentation.common.Notifier
import ru.matule.uikit.components.bottomsheet.MatuleBottomSheet
import ru.matule.uikit.components.button.MatuleButton
import ru.matule.uikit.theme.MatuleTheme

/**
 * Карточка товара: сразу показывает данные из списка, затем загружает полное описание.
 *
 * @param initial товар из списка.
 * @param getProduct получение описания товара.
 * @param notifier уведомления об ошибках.
 */
class ProductSheetViewModel(
    initial: Product,
    getProduct: GetProductUseCase,
    notifier: Notifier,
) : ViewModel() {

    private val _product = MutableStateFlow(initial)

    /** Товар. */
    val product: StateFlow<Product> = _product.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getProduct(initial.id)) {
                is AppResult.Success -> _product.value = result.data
                is AppResult.Failure -> notifier.showError(result.error)
            }
        }
    }
}

/**
 * BottomSheet карточки описания: название, описание, примерный расход, «Добавить за N ₽».
 *
 * @param product товар из списка.
 * @param onAdd добавить в корзину.
 * @param onDismiss закрыть карточку.
 */
@Composable
fun ProductSheet(
    product: Product,
    onAdd: (Product) -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModel = matuleViewModel(key = "product_${product.id}") {
        ProductSheetViewModel(product, getProduct, notifier)
    }
    val current by viewModel.product.collectAsStateWithLifecycle()
    val colors = MatuleTheme.colors
    val typography = MatuleTheme.typography

    MatuleBottomSheet(title = current.title, onDismiss = onDismiss) {
        Text(text = "Описание", style = typography.captionRegular, color = colors.description)
        Spacer(Modifier.height(8.dp))
        Text(text = current.description, style = typography.textRegular, color = colors.black)
        if (current.consumption.isNotBlank()) {
            Spacer(Modifier.height(24.dp))
            Text(text = "Примерный расход:", style = typography.captionRegular, color = colors.description)
            Spacer(Modifier.height(4.dp))
            Text(text = current.consumption, style = typography.textMedium, color = colors.black)
        }
        Spacer(Modifier.height(24.dp))
        MatuleButton(
            text = "Добавить за ${Formatters.price(current.price)}",
            onClick = {
                onAdd(current)
                dismiss()
            },
        )
    }
}
