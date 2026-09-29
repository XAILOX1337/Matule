package ru.matule.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.FragmentActivity
import ru.matule.app.di.LocalAppContainer
import ru.matule.app.presentation.navigation.MatuleAppRoot
import ru.matule.uikit.theme.MatuleTheme

/**
 * Единственная Activity. Наследуется от FragmentActivity, потому что системный диалог
 * биометрии (BiometricPrompt) работает через фрагменты.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Макет только светлый: тёмные иконки системных панелей на прозрачном фоне в любой теме телефона.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        val container = (application as MatuleApp).container
        setContent {
            MatuleTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    MatuleAppRoot(notifier = container.notifier)
                }
            }
        }
    }
}
