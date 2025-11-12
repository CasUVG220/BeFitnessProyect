package com.befitnessapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object Onboarding
@Serializable object Login
@Serializable object Register

@Serializable object Home
@Serializable object Dashboard
@Serializable object Library
@Serializable object MuscleMap
@Serializable object WorkoutLog
@Serializable object Recommendations
@Serializable object Routines
@Serializable object Calendar
@Serializable object Profile
@Serializable object Settings

@Serializable data class AddLog(val date: String? = null)
