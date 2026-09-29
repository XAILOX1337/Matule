package com.example.matuleclothes.presentation.Splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF78B4D8),
                        Color(0xFF2E4FE6),
                        Color(0xFF78B4D8)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Matule",
            color = Color.White,
            fontFamily = FontFamily(
                Font(com.example.uikit.R.font.sf_pro_display_regular)
            ),
            fontSize = 40.sp,
            fontWeight = FontWeight(400)
        )
    }
}
