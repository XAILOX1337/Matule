package ru.matule.app.domain.repository

/**
 * Локальные напоминания (уведомления).
 */
interface ReminderScheduler {
    /** Приложение открыто — напоминание «давно не заходили» больше не нужно. */
    fun onAppStarted()

    /** Приложение свёрнуто — через минуту напомнить о себе (один раз до следующего входа). */
    fun onAppStopped()

    /**
     * Активность в проекте: открытие или действие пользователя.
     * Если дальше 3 минуты ничего не происходит — напоминание о проекте.
     *
     * @param projectId идентификатор проекта.
     * @param projectTitle название для текста уведомления.
     */
    fun onProjectActivity(projectId: String, projectTitle: String)

    /** Отменить все напоминания (например, при выходе из аккаунта). */
    fun cancelAll()
}

/**
 * Аналитика действий пользователя (Яндекс AppMetrica или журнал).
 */
interface AnalyticsTracker {
    /**
     * Отправляет событие.
     *
     * @param event имя события.
     * @param params параметры события.
     */
    fun track(event: String, params: Map<String, String> = emptyMap())
}
