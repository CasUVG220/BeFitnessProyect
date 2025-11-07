package com.befitnessapp.ui.screens.routines.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface RoutinesRoute {
    @Serializable data object Home : RoutinesRoute
    @Serializable data object List : RoutinesRoute
    @Serializable data class Builder(val routineId: Long? = null) : RoutinesRoute
}
