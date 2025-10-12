package com.befitnessapp.ui.navigation

import java.time.LocalDate

sealed class Route(val path: String) {
    object Onboarding : Route("onboarding")
    object Login : Route("login")
    object Register : Route("register")
    object Home : Route("home")
    object Dashboard : Route("dashboard")
    object Library : Route("library")
    object MuscleMap : Route("musclemap")
    object WorkoutLog : Route("workoutlog")
    object AddLog : Route("addlog")
    object Recommendations : Route("recommendations")
    object Routines : Route("routines")
    object Calendar : Route("calendar")
    object Profile : Route("profile")
    object Settings : Route("settings")
}


fun addLogWithDate(date: LocalDate?): String =
    if (date != null) "${Route.AddLog.path}?date=$date" else Route.AddLog.path
