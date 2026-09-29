package com.example.matuleclothes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.matuleclothes.presentation.Splash.SplashScreen
import com.example.matuleclothes.ui.theme.MatuleClothesTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MatuleClothesTheme {
                SplashScreen()
            }
        }
    }
}
