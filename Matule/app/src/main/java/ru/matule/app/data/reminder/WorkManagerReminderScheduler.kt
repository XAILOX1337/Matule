package ru.matule.app.data.reminder

import android.content.Context
import android.os.SystemClock
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import ru.matule.app.domain.repository.ReminderScheduler
import ru.matule.app.domain.repository.UserDataCleaner
import java.util.concurrent.TimeUnit

/**
 * Напоминания через WorkManager: переживают закрытие приложения и перезагрузку телефона.
 *
 * @param context контекст приложения.
 */
class WorkManagerReminderScheduler(context: Context) : ReminderScheduler, UserDataCleaner {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    @Volatile
    private var lastProjectId: String? = null

    @Volatile
    private var lastProjectScheduleAt = 0L

    override fun onAppStarted() {
        workManager.cancelUniqueWork(APP_WORK)
    }

    override fun onAppStopped() {
        // KEEP: одно напоминание до следующего входа, даже если приложение сворачивали несколько раз.
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(APP_DELAY_MINUTES, TimeUnit.MINUTES)
            .setInputData(workDataOf(ReminderWorker.KEY_KIND to ReminderWorker.KIND_APP))
            .build()
        workManager.enqueueUniqueWork(APP_WORK, ExistingWorkPolicy.KEEP, request)
    }

    override fun onProjectActivity(projectId: String, projectTitle: String) {
        // Касания приходят очень часто: переносим напоминание не чаще раза в несколько секунд.
        val now = SystemClock.elapsedRealtime()
        if (projectId == lastProjectId && now - lastProjectScheduleAt < THROTTLE_MS) return
        lastProjectId = projectId
        lastProjectScheduleAt = now
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(PROJECT_DELAY_MINUTES, TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_KIND to ReminderWorker.KIND_PROJECT,
                    ReminderWorker.KEY_PROJECT_TITLE to projectTitle,
                ),
            )
            .build()
        workManager.enqueueUniqueWork(PROJECT_WORK, ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancelAll() {
        workManager.cancelUniqueWork(APP_WORK)
        workManager.cancelUniqueWork(PROJECT_WORK)
        lastProjectId = null
    }

    override suspend fun clearUserData() = cancelAll()

    private companion object {
        const val APP_WORK = "reminder_app_inactive"
        const val PROJECT_WORK = "reminder_project_inactive"
        const val APP_DELAY_MINUTES = 1L
        const val PROJECT_DELAY_MINUTES = 3L
        const val THROTTLE_MS = 5_000L
    }
}
