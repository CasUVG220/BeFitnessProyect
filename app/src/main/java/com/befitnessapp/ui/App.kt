package com.befitnessapp.ui

import androidx.compose.runtime.Composable
import com.befitnessapp.ui.navigation.AppNavHost
import com.befitnessapp.ui.theme.BeFitnessTheme

@Composable
fun App() {
    BeFitnessTheme {
        AppNavHost()
    }
}
