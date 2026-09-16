package com.exposuretrackingsystem.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF6EA8FE),
    secondary = androidx.compose.ui.graphics.Color(0xFF8FD3FF),
    tertiary = androidx.compose.ui.graphics.Color(0xFFB7D7FF)
)

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF2F6FED),
    secondary = androidx.compose.ui.graphics.Color(0xFF4DA3FF),
    tertiary = androidx.compose.ui.graphics.Color(0xFF6AA5FF)
)

@Composable
fun ExposureTrackingSystemTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
