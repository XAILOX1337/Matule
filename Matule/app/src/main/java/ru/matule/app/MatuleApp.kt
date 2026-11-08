package ru.matule.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import ru.matule.app.di.AppContainer

/**
 * Приложение Matule. Держит контейнер зависимостей, создаёт канал уведомлений,
 * активирует AppMetrica (если задан API-ключ) и следит, когда приложение
 * открывают и сворачивают (для напоминания «давно не заходили»).
 */
class MatuleApp : Application() {

    /** Зависимости приложения. */
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        activateAppMetrica()
        container.reminderNotifications.createChannel()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) = container.reminderScheduler.onAppStarted()

                override fun onStop(owner: LifecycleOwner) = container.reminderScheduler.onAppStopped()
            },
        )
    }

    /**
     * Активирует AppMetrica. Ключ берётся из `BuildConfig.APPMETRICA_API_KEY`
     * (свойство `MATULE_APPMETRICA_KEY` в `gradle.properties`); без ключа SDK не
     * инициализируется, а события пишет [ru.matule.app.data.analytics.LogcatAnalyticsTracker].
     */
    private fun activateAppMetrica() {
        if (BuildConfig.APPMETRICA_API_KEY.isBlank()) return
        val config = YandexMetricaConfig.newConfigBuilder(BuildConfig.APPMETRICA_API_KEY).build()
        YandexMetrica.activate(this, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }
}
