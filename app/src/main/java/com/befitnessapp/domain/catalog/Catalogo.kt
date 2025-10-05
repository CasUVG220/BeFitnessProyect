package com.befitnessapp.domain.catalog

object Catalogo {
    val allGroups = groups
    val allMuscles = muscles
    val allExercises = exercises

    val muscleById: Map<Int, Muscle> = allMuscles.associateBy { it.id }
    val groupById: Map<Int, MuscleGroup> = allGroups.associateBy { it.id }

    // índice ejercicio→músculos target
    val targetsByExerciseId: Map<Int, List<ExerciseTarget>> =
        allExercises.associate { it.id to it.targets }

    // índice músculo→ejercicios implicados
    val exercisesByMuscleId: Map<Int, List<Exercise>> =
        allMuscles.associate { m ->
            val list = allExercises.filter { ex -> ex.targets.any { it.muscleId == m.id } }
            m.id to list
        }

    fun searchExercises(
        query: String = "",
        groupId: Int? = null,
        muscleId: Int? = null,
        pattern: Pattern? = null
    ): List<Exercise> {
        val q = query.trim().lowercase()
        return allExercises
            .asSequence()
            .filter { ex -> if (q.isBlank()) true else ex.name.lowercase().contains(q) }
            .filter { ex ->
                when {
                    muscleId != null -> ex.targets.any { it.muscleId == muscleId }
                    groupId != null -> ex.targets.any { t -> muscleById[t.muscleId]?.groupId == groupId }
                    else -> true
                }
            }
            .filter { ex -> pattern?.let { ex.pattern == it } ?: true }
            .sortedBy { it.name }
            .toList()
    }
}
