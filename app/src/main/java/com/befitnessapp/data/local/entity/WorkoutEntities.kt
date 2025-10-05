package com.befitnessapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Entity(
    tableName = "workout",
    indices = [Index("date")]
)
data class WorkoutEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val notes: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deleted: Boolean = false
)

@Entity(
    tableName = "workout_set",
    indices = [Index("workoutId"), Index("exerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val workoutId: String,
    val exerciseId: Int,        // ID del catálogo
    val setIndex: Int,          // 1,2,3... dentro del ejercicio de ese día
    val reps: Int,
    val weight: Float,          // en kg o lb (según pref), lo resolveremos en UI
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deleted: Boolean = false
)
