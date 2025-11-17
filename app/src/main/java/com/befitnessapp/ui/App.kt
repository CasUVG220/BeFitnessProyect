package com.befitnessapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.befitnessapp.prefs.AppSettings
import com.befitnessapp.prefs.AppTheme
import com.befitnessapp.prefs.SettingsState
import com.befitnessapp.ui.localization.LocalStrings
import com.befitnessapp.ui.localization.stringsForLanguage
import com.befitnessapp.ui.navigation.AppNavHost
import com.befitnessapp.ui.theme.BeFitnessTheme

@Composable
fun App() {
    val context = LocalContext.current
    val settingsFlow = remember { AppSettings.observe(context) }
    val settingsState by settingsFlow.collectAsState(initial = SettingsState())

    val isDarkTheme = when (settingsState.theme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val strings = remember(settingsState.language) {
        stringsForLanguage(settingsState.language)
    }

    CompositionLocalProvider(LocalStrings provides strings) {
        BeFitnessTheme(darkTheme = isDarkTheme) {
            AppNavHost()
        }
    }
}
