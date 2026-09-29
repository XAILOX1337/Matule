package ru.matule.app.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.matule.app.MatuleApp

/**
 * Фоновая задача WorkManager: показывает напоминание, если пользователь не отключил уведомления.
 *
 * @param context контекст.
 * @param params параметры: вид напоминания и название проекта.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as MatuleApp).container
        if (!container.isNotificationsEnabled()) return Result.success()
        val notifications = container.reminderNotifications
        when (inputData.getString(KEY_KIND)) {
            KIND_APP -> notifications.showAppReminder()
            KIND_PROJECT -> notifications.showProjectReminder(inputData.getString(KEY_PROJECT_TITLE).orEmpty())
        }
        return Result.success()
    }

    companion object {
        /** Вид напоминания. */
        const val KEY_KIND = "kind"

        /** Название проекта. */
        const val KEY_PROJECT_TITLE = "project_title"

        /** Приложение давно не открывали. */
        const val KIND_APP = "app"

        /** Нет активности в проекте. */
        const val KIND_PROJECT = "project"
    }
}
