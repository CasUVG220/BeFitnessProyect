package com.befitnessapp.domain.recommendation

import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.catalog.Muscle
import com.befitnessapp.domain.catalog.*


data class WeeklyTargets(val perCanonicalMuscle: Map<Int, Float>) {
    fun targetFor(muscleId: Int): Float {
        val canonical = canonicalId(muscleId)
        return perCanonicalMuscle[canonical] ?: 0f
    }
}

fun canonicalId(muscleId: Int): Int {
    val m: Muscle? = Catalogo.muscleById[muscleId]
    return m?.parentId ?: muscleId
}

fun defaultWeeklyTargets(): WeeklyTargets {
    val groupTotals = mapOf(
        GRUPO_PECHO   to 8000f,
        GRUPO_ESPALDA to 9000f,
        GRUPO_HOMBROS to 6000f,
        GRUPO_PIERNAS to 11000f,
        GRUPO_GLUTEOS to 7000f,
        GRUPO_BRAZOS  to 5000f,
        GRUPO_CORE    to 4000f
    )

    // Solo músculos canónicos
    val canonicalMuscles = Catalogo.allMuscles.filter { it.parentId == null }

    val perGroupCounts: Map<Int, Int> = canonicalMuscles
        .groupBy { it.groupId }
        .mapValues { (_, list) -> list.size.coerceAtLeast(1) }

    val map = mutableMapOf<Int, Float>()
    canonicalMuscles.forEach { m ->
        val total = groupTotals[m.groupId] ?: 0f
        val count = perGroupCounts[m.groupId] ?: 1
        map[m.id] = total / count
    }
    return WeeklyTargets(map)
}

// ponderaciond e musuclos
const val PRIMARY_WEIGHT: Float = 1.0f
const val SECONDARY_WEIGHT: Float = 0.5f
