package ru.matule.app.presentation.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.matule.app.domain.result.AppError

/**
 * Уведомление для пользователя (показывается компонентом SnackBar из UIKit).
 *
 * @property title заголовок.
 * @property text текст или `null`.
 * @property durationMillis время показа; `null` — до закрытия кнопкой.
 */
data class UiMessage(
    val title: String,
    val text: String?,
    val durationMillis: Long?,
)

/**
 * Единая точка показа уведомлений: ViewModel любого экрана сообщает об ошибке или успехе,
 * а корневой экран приложения показывает SnackBar поверх навигации.
 */
class Notifier {

    private val events = MutableSharedFlow<UiMessage>(extraBufferCapacity = 8)

    /** Поток уведомлений для корневого экрана. */
    val messages: SharedFlow<UiMessage> = events.asSharedFlow()

    /** Ошибка сервера, сети или проверки данных. Закрывается сама или кнопкой «×». */
    fun showError(text: String, title: String = ERROR_TITLE) {
        events.tryEmit(UiMessage(title, text, ERROR_DURATION_MS))
    }

    /** Ошибка из бизнес-логики. */
    fun showError(error: AppError) = showError(error.message)

    /** Успешное действие, например оформленный заказ (5 секунд). */
    fun showSuccess(title: String, text: String? = null, durationMillis: Long = SUCCESS_DURATION_MS) {
        events.tryEmit(UiMessage(title, text, durationMillis))
    }

    /** Справочное сообщение. */
    fun showInfo(title: String, text: String? = null) {
        events.tryEmit(UiMessage(title, text, INFO_DURATION_MS))
    }

    private companion object {
        const val ERROR_TITLE = "Произошла ошибка"
        const val ERROR_DURATION_MS = 4_000L
        const val SUCCESS_DURATION_MS = 5_000L
        const val INFO_DURATION_MS = 3_000L
    }
}
