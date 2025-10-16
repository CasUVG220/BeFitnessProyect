package com.befitnessapp.domain.recommendation

import com.befitnessapp.data.local.dao.WorkoutWithSets
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.catalog.TargetRole


data class MuscleCoverage(
    val coverageByCanonical: Map<Int, Float> // canonicalId -> coverage (0..∞)
)

data class ExerciseScore(
    val exerciseId: Int,
    val name: String,
    val score: Float,
    val deficits: Map<Int, Float> // canonicalId -> déficit (0..1) que atiende este ejercicio
)


fun computeCoverage(
    history: List<WorkoutWithSets>,
    targets: WeeklyTargets
): MuscleCoverage {
    val actualByCanonical = mutableMapOf<Int, Float>()

    history.forEach { ww ->
        ww.sets.forEach { s ->
            val vol = s.reps * s.weight
            val targetsForExercise = Catalogo.targetsByExerciseId[s.exerciseId].orEmpty()
            targetsForExercise.forEach { t ->
                val canonical = canonicalId(t.muscleId)
                val roleFactor = if (t.role == TargetRole.PRIMARY) PRIMARY_WEIGHT else SECONDARY_WEIGHT
                val add = (vol * t.weight * roleFactor).toFloat()
                actualByCanonical[canonical] = (actualByCanonical[canonical] ?: 0f) + add
            }
        }
    }

    val cov = targets.perCanonicalMuscle.mapValues { (muscle, tgt) ->
        if (tgt <= 0f) 1f else (actualByCanonical[muscle] ?: 0f) / tgt
    }

    return MuscleCoverage(cov)
}

// Puntúa un ejercicio según cuánto empuja donde hay déficit que depende del trabajo hecho de este musculo
fun scoreExercise(
    exerciseId: Int,
    coverage: MuscleCoverage
): ExerciseScore {
    val ex = Catalogo.allExercises.firstOrNull { it.id == exerciseId }
        ?: return ExerciseScore(exerciseId, "Ejercicio $exerciseId", 0f, emptyMap())

    var total = 0f
    val deficits = mutableMapOf<Int, Float>()

    Catalogo.targetsByExerciseId[exerciseId].orEmpty().forEach { t ->
        val canonical = canonicalId(t.muscleId)
        val cov = coverage.coverageByCanonical[canonical] ?: 1f
        val deficit = (1f - cov).coerceAtLeast(0f) // 0..1
        if (deficit > 0f) {
            val roleFactor = if (t.role == TargetRole.PRIMARY) PRIMARY_WEIGHT else SECONDARY_WEIGHT
            total += deficit * t.weight.toFloat() * roleFactor
            deficits[canonical] = deficit
        }
    }

    return ExerciseScore(
        exerciseId = exerciseId,
        name = ex.name,
        score = total,
        deficits = deficits
    )
}

fun rankExercises(
    history: List<WorkoutWithSets>,
    targets: WeeklyTargets,
    topN: Int = 5
): List<ExerciseScore> {
    val cov = computeCoverage(history, targets)
    return Catalogo.allExercises.asSequence()
        .map { scoreExercise(it.id, cov) }
        .filter { it.score > 0f }
        .sortedByDescending { it.score }
        .take(topN)
        .toList()
}
