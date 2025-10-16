package com.befitnessapp.domain.recommendation

import com.befitnessapp.data.local.dao.ExerciseAgg
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.catalog.TargetRole

class RecommendationEngine(
    private val primaryRoleWeight: Float = 1.0f,
    private val secondaryRoleWeight: Float = 0.5f
) {
    fun computeMuscleLoads(aggs: List<ExerciseAgg>): Map<Int, Float> {
        val loads = HashMap<Int, Float>()
        for (a in aggs) {
            val targets = Catalogo.targetsByExerciseId[a.exerciseId].orEmpty()
            for (t in targets) {
                val roleW = if (t.role == TargetRole.PRIMARY) primaryRoleWeight else secondaryRoleWeight
                val delta = (a.volume * roleW * t.weight).toFloat()
                loads[t.muscleId] = (loads[t.muscleId] ?: 0f) + delta
            }
        }
        return loads
    }

    /**
     * Devuelve hasta K ejercicios que favorecen los músculos menos trabajados
     * y de preferencia NO repetidos recientemente.
     */
    fun suggestExercises(aggs: List<ExerciseAgg>, k: Int = 3): List<Int> {
        if (k <= 0) return emptyList()
        val recent = aggs.map { it.exerciseId }.toSet()

        val loads = computeMuscleLoads(aggs)
        val targetMuscles: List<Int> = if (loads.isEmpty()) {
            // Sin historial: diversificar patrones por defecto
            emptyList()
        } else {
            loads.toList().sortedBy { it.second }.take(4).map { it.first }
        }

        val candidates = Catalogo.allExercises

        fun score(exId: Int): Float {
            val targets = Catalogo.targetsByExerciseId[exId].orEmpty()
            var s = 0f
            for (t in targets) {
                val need = 1f / (1f + (loads[t.muscleId] ?: 0f)) // más bajo => más necesidad
                val roleW = if (t.role == TargetRole.PRIMARY) primaryRoleWeight else secondaryRoleWeight
                val focus = if (t.muscleId in targetMuscles) 1.0f else 0.6f
                s += need * roleW * focus * t.weight.toFloat()
            }
            if (exId in recent) s *= 0.7f // penaliza repetición
            return s
        }

        val ranked = candidates
            .asSequence()
            .sortedByDescending { score(it.id) }
            .map { it.id }
            .distinct()
            .take(k)
            .toList()

        // Fallback cuando no hay historial: variedad simple
        if (ranked.isNotEmpty()) return ranked
        val patternsPick = candidates
            .groupBy { it.pattern }
            .values
            .map { it.first().id }
        return patternsPick.take(k)
    }
}
