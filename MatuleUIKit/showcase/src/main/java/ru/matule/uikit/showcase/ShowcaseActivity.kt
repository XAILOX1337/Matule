package ru.matule.uikit.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.matule.uikit.theme.MatuleTheme

/**
 * Демо-приложение UIKit: все компоненты библиотеки во всех состояниях с подписями.
 */
class ShowcaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MatuleTheme {
                ShowcaseScreen()
            }
        }
    }
}
