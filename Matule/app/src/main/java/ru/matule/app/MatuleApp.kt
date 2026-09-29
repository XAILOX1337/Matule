package ru.matule.app

import android.app.Application
import ru.matule.app.di.AppContainer

/**
 * Приложение Matule. Держит контейнер зависимостей на всё время жизни процесса.
 */
class MatuleApp : Application() {

    /** Зависимости приложения. */
    val container: AppContainer by lazy { AppContainer(this) }
}
