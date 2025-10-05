package com.befitnessapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = md_theme_primary,
    onPrimary = md_theme_onPrimary,
    background = md_theme_background,
    onBackground = md_theme_onBackground
)

private val DarkColors = darkColorScheme()

@Composable
fun BeFitnessTheme(
    darkTheme: Boolean = false, // luego: seguir el sistema
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
