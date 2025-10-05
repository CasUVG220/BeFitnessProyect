package com.befitnessapp.domain.catalog

enum class Pattern { PRESS, ROW, PULL, OVERHEAD, RAISE, SQUAT, HINGE, CALF, THRUST, CURL, EXTENSION }
enum class RepGoal { HIPERTROFIA, FUERZA, RESISTENCIA }
enum class TargetRole { PRIMARY, SECONDARY }

data class MuscleGroup(
    val id: Int,
    val name: String,
)

data class Muscle(
    val id: Int,
    val groupId: Int,
    val name: String,
    val parentId: Int? = null // null => músculo “canónico”
)

data class RepRange(val min: Int, val max: Int)

data class ExerciseTarget(
    val muscleId: Int,
    val weight: Double, // impacto relativo total de primarios y secundarios (≈ suma 1.0)
    val role: TargetRole
)

data class Exercise(
    val id: Int,
    val name: String,
    val pattern: Pattern,
    val repRangeByGoal: Map<RepGoal, RepRange>,
    val contraindications: List<String> = emptyList(),
    val targets: List<ExerciseTarget>
)
