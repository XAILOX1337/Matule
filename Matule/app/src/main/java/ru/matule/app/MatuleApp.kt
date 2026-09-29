package ru.matule.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import ru.matule.app.di.AppContainer

/**
 * Приложение Matule. Держит контейнер зависимостей, создаёт канал уведомлений
 * и следит, когда приложение открывают и сворачивают (для напоминания «давно не заходили»).
 */
class MatuleApp : Application() {

    /** Зависимости приложения. */
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        container.reminderNotifications.createChannel()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) = container.reminderScheduler.onAppStarted()

                override fun onStop(owner: LifecycleOwner) = container.reminderScheduler.onAppStopped()
            },
        )
    }
}
