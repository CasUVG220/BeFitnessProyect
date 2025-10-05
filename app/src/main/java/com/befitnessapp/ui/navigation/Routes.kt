package com.befitnessapp.ui.navigation

sealed class Route(val path: String) {
    // Pre-login
    data object Onboarding : Route("onboarding")
    data object Login : Route("login")
    data object Register : Route("register")

    // Post-login
    data object Home : Route("home")
    data object Dashboard : Route("dashboard")
    data object Library : Route("library")
    data object MuscleMap : Route("musclemap")
    data object WorkoutLog : Route("workoutlog")
    data object AddLog : Route("addlog")
    data object Recommendations : Route("recommendations")
    data object Routines : Route("routines")
    data object Calendar : Route("calendar")
    data object Profile : Route("profile")
    data object Settings : Route("settings")
}
