package ru.matule.app.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ru.matule.app.MainActivity
import ru.matule.app.R

/**
 * Локальные уведомления-напоминания: канал и показ.
 *
 * @param context контекст приложения.
 */
class ReminderNotifications(context: Context) {

    private val appContext = context.applicationContext

    /** Создаёт канал уведомлений (повторный вызов безопасен). */
    fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Напоминания", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Напоминания о приложении и проектах"
        }
        appContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Приложение не открывали больше минуты. */
    fun showAppReminder() = show(
        id = APP_REMINDER_ID,
        title = "Matule ждёт вас",
        text = "Вы не заходили в приложение больше минуты. Загляните в новые описания!",
    )

    /**
     * В проекте нет активности больше 3 минут.
     *
     * @param projectTitle название проекта.
     */
    fun showProjectReminder(projectTitle: String) = show(
        id = PROJECT_REMINDER_ID,
        title = "Проект «$projectTitle»",
        text = "Вы не возвращались к проекту больше 3 минут. Продолжим?",
    )

    private fun show(id: Int, title: String, text: String) {
        val granted = ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) return
        val intent = Intent(appContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(appContext, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(appContext).notify(id, notification)
    }

    private companion object {
        const val CHANNEL_ID = "reminders"
        const val APP_REMINDER_ID = 1001
        const val PROJECT_REMINDER_ID = 1002
    }
}
