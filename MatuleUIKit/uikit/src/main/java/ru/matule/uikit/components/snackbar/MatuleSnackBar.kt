package ru.matule.uikit.components.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.matule.uikit.components.button.MatuleBubbleButton
import ru.matule.uikit.components.card.MatuleCard
import ru.matule.uikit.icons.MatuleIcons
import ru.matule.uikit.theme.MatuleTheme

/**
 * Уведомление (SnackBar): заголовок, текст и круглая синяя кнопка «×» для принудительного закрытия.
 *
 * @param title заголовок, например «Произошла ошибка».
 * @param message текст уведомления; `null` — только заголовок.
 * @param onClose нажатие кнопки закрытия.
 * @param modifier модификатор уведомления.
 */
@Composable
fun MatuleSnackBar(
    title: String,
    message: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MatuleTheme.colors
    MatuleCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 12.dp, bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MatuleTheme.typography.headlineMedium, color = colors.black)
                if (!message.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(text = message, style = MatuleTheme.typography.textRegular, color = colors.black)
                }
            }
            Spacer(Modifier.width(12.dp))
            MatuleBubbleButton(
                icon = MatuleIcons.Close,
                onClick = onClose,
                size = 24.dp,
                iconSize = 16.dp,
                background = colors.accent,
                tint = colors.white,
                contentDescription = "Закрыть уведомление",
            )
        }
    }
}

/**
 * Данные показываемого уведомления.
 *
 * @property id уникальный номер показа (чтобы одинаковые тексты показывались повторно).
 * @property title заголовок.
 * @property message текст.
 * @property durationMillis через сколько миллисекунд скрыть; `null` — висит, пока не закроют кнопкой.
 */
@Immutable
data class MatuleSnackBarData(
    val id: Long,
    val title: String,
    val message: String?,
    val durationMillis: Long?,
)

/**
 * Состояние хоста уведомлений: какое уведомление показано сейчас.
 */
@Stable
class MatuleSnackBarState {
    private var counter = 0L

    /** Текущее уведомление; `null` — ничего не показано. */
    var current: MatuleSnackBarData? by mutableStateOf(null)
        private set

    /**
     * Показывает уведомление, заменяя текущее.
     *
     * @param title заголовок.
     * @param message текст.
     * @param durationMillis время показа; `null` — до закрытия кнопкой.
     */
    fun show(title: String, message: String? = null, durationMillis: Long? = null) {
        counter += 1
        current = MatuleSnackBarData(counter, title, message, durationMillis)
    }

    /** Скрывает текущее уведомление. */
    fun dismiss() {
        current = null
    }
}

/**
 * Создаёт и запоминает [MatuleSnackBarState].
 *
 * @return состояние хоста уведомлений.
 */
@Composable
fun rememberMatuleSnackBarState(): MatuleSnackBarState = remember { MatuleSnackBarState() }

/**
 * Хост уведомлений: показывает [MatuleSnackBar] с анимацией сверху экрана
 * и скрывает его по таймеру или по кнопке закрытия.
 *
 * @param state состояние хоста.
 * @param modifier модификатор (обычно выравнивание и отступы от краёв).
 */
@Composable
fun MatuleSnackBarHost(
    state: MatuleSnackBarState,
    modifier: Modifier = Modifier,
) {
    val data = state.current
    // Последнее показанное уведомление нужно, чтобы доиграть анимацию скрытия.
    val lastShown = remember { LastShownHolder() }
    if (data != null) lastShown.value = data

    LaunchedEffect(data?.id) {
        val shown = data ?: return@LaunchedEffect
        val duration = shown.durationMillis ?: return@LaunchedEffect
        delay(duration)
        if (state.current?.id == shown.id) state.dismiss()
    }

    AnimatedVisibility(
        visible = data != null,
        modifier = modifier,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
    ) {
        val shown = data ?: lastShown.value
        if (shown != null) {
            MatuleSnackBar(title = shown.title, message = shown.message, onClose = { state.dismiss() })
        }
    }
}

/** Хранит последнее показанное уведомление без лишних рекомпозиций. */
private class LastShownHolder {
    var value: MatuleSnackBarData? = null
}
