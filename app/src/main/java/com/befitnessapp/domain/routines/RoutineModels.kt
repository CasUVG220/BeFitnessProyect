package com.befitnessapp.domain.routines

data class Routine(
    val id: Long = 0L,
    val name: String,
    val isArchived: Boolean = false
)

data class RoutineSetTemplate(val reps: Int, val weight: Float)

data class RoutineExerciseTemplate(
    val exerciseId: Int,
    val displayName: String,
    val sets: List<RoutineSetTemplate>
)

data class RoutineDetail(
    val routine: Routine,
    val exercises: List<RoutineExerciseTemplate>
)
