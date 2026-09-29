package ru.matule.uikit.components.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.matule.uikit.components.button.MatuleBubbleButton
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/**
 * Возможности содержимого BottomSheet.
 */
fun interface MatuleBottomSheetScope {
    /** Плавно скрывает окно, после чего вызывается `onDismiss`. */
    fun dismiss()
}

/**
 * Модальное окно снизу (Модальные окна → Modal). Высота — по размеру контента;
 * если контент не помещается, окно раскрывается на весь экран и содержимое прокручивается.
 *
 * @param onDismiss вызывается, когда окно закрыто (свайпом, тапом по затемнению, кнопкой «×» или [MatuleBottomSheetScope.dismiss]).
 * @param modifier модификатор окна.
 * @param title заголовок слева вверху; `null` — без заголовка.
 * @param showCloseButton показывать ли круглую кнопку «×» справа вверху.
 * @param content содержимое окна; через [MatuleBottomSheetScope] его можно закрыть с анимацией.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatuleBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    showCloseButton: Boolean = true,
    content: @Composable MatuleBottomSheetScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val sheetScope = remember(sheetState) {
        MatuleBottomSheetScope {
            scope.launch { sheetState.hide() }.invokeOnCompletion { currentOnDismiss() }
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = MatuleTheme.shapes.sheet,
        containerColor = MatuleTheme.colors.white,
        scrimColor = MatuleTheme.colors.overlay,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            if (title != null || showCloseButton) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = title.orEmpty(),
                        style = MatuleTheme.typography.title2Semibold,
                        color = MatuleTheme.colors.black,
                        modifier = Modifier.weight(1f),
                    )
                    if (showCloseButton) {
                        Spacer(Modifier.width(12.dp))
                        MatuleBubbleButton(
                            icon = MatuleIcons.Close,
                            onClick = { sheetScope.dismiss() },
                            size = 24.dp,
                            iconSize = 16.dp,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            sheetScope.content()
        }
    }
}
