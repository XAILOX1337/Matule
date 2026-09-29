package ru.matule.app.data.analytics

import android.util.Log
import ru.matule.app.domain.repository.AnalyticsTracker

/**
 * Аналитика в журнал Logcat (тег `MatuleAnalytics`). Заменяется на AppMetrica в DI,
 * экраны при этом не меняются.
 */
class LogcatAnalyticsTracker : AnalyticsTracker {
    override fun track(event: String, params: Map<String, String>) {
        Log.i(TAG, if (params.isEmpty()) event else "$event $params")
    }

    private companion object {
        const val TAG = "MatuleAnalytics"
    }
}
