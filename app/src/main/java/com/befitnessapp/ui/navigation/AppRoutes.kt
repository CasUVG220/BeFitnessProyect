package com.befitnessapp.ui.navigation

import kotlinx.serialization.Serializable

// Rutas tipadas
@Serializable
sealed interface AppRoute {
    // Onboarding / Auth
    @Serializable data object Onboarding : AppRoute
    @Serializable data object Login : AppRoute
    @Serializable data object Register : AppRoute

    // Home / Tabs
    @Serializable data object Home : AppRoute
    @Serializable data object Dashboard : AppRoute
    @Serializable data object Library : AppRoute
    @Serializable data object MuscleMap : AppRoute
    @Serializable data object WorkoutLog : AppRoute
    @Serializable data object Recommendations : AppRoute
    @Serializable data object Routines : AppRoute
    @Serializable data object Calendar : AppRoute
    @Serializable data object Profile : AppRoute
    @Serializable data object Settings : AppRoute

    @Serializable data class AddLog(val date: String? = null) : AppRoute
}
