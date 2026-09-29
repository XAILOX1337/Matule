package com.example.matuleclothes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


private val MatuleDarkColorScheme = darkColorScheme(
    primary = MatuleAccent,
    onPrimary = MatuleWhite,
    primaryContainer = MatuleAccentInactive,
    onPrimaryContainer = MatuleCarbonDarkest,
    secondary = MatuleAccentInactive,
    onSecondary = MatuleCarbonDarkest,
    background = MatuleCarbonDarkest,
    onBackground = MatuleLabelPrimaryDark,
    surface = MatuleCarbonDarkest,
    onSurface = MatuleLabelPrimaryDark,
    surfaceVariant = MatuleCarbonNeutral300,
    onSurfaceVariant = MatuleCaption,
    outline = MatuleCarbonNeutral300,
    error = MatuleError,
    onError = MatuleWhite
)

private val MatuleLightColorScheme = lightColorScheme(
    primary = MatuleAccent,
    onPrimary = MatuleWhite,
    primaryContainer = MatuleAccentInactive,
    onPrimaryContainer = MatuleAccent,
    secondary = MatuleInputBackground,
    onSecondary = MatuleLabelPrimaryLight,
    background = MatuleWhite,
    onBackground = MatuleLabelPrimaryLight,
    surface = MatuleWhite,
    onSurface = MatuleLabelPrimaryLight,
    surfaceVariant = MatuleInputBackground,
    onSurfaceVariant = MatuleDescription,
    outline = MatuleInputStroke,
    error = MatuleError,
    onError = MatuleWhite

    /* Other default colors to override
    onTertiary = Color.White,
    */
)

@Composable
fun MatuleClothesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> MatuleDarkColorScheme
        else -> MatuleLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
