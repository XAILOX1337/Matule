package ru.matule.app.presentation.common

import androidx.compose.ui.graphics.Color

/**
 * Цвет из строки «#RRGGBB» (например, цвет баннера акции с сервера).
 *
 * @param hex цвет в формате #RRGGBB или #AARRGGBB.
 * @param fallback цвет, если строка не читается.
 */
fun parseHexColor(hex: String, fallback: Color): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex.trim())) }.getOrDefault(fallback)
