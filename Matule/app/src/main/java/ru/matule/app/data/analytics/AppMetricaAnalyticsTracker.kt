package ru.matule.app.data.analytics

import com.yandex.metrica.YandexMetrica
import ru.matule.app.domain.repository.AnalyticsTracker

/**
 * Аналитика через AppMetrica (Яндекс). События экрана «Создать проект» уходят
 * в кабинет метрики, по ним строится диаграмма активности для презентации.
 * Подключается в DI вместо [LogcatAnalyticsTracker], когда задан API-ключ.
 */
class AppMetricaAnalyticsTracker : AnalyticsTracker {

    override fun track(event: String, params: Map<String, String>) {
        if (params.isEmpty()) {
            YandexMetrica.reportEvent(event)
        } else {
            YandexMetrica.reportEvent(event, params)
        }
    }
}
