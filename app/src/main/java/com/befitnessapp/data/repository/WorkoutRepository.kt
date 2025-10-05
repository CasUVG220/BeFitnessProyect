package com.befitnessapp.data.repository

import com.befitnessapp.data.local.dao.WorkoutDao
import com.befitnessapp.data.local.dao.WorkoutWithSets
import com.befitnessapp.data.local.entity.WorkoutEntity
import com.befitnessapp.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class SetInput(
    val exerciseId: Int,
    val reps: Int,
    val weight: Float
)

class WorkoutRepository(private val dao: WorkoutDao) {


    suspend fun createWorkout(
        date: LocalDate,
        notes: String?,
        setsByExercise: Map<Int, List<Pair<Int, Float>>>
    ) {
        val workoutId = UUID.randomUUID().toString()
        val now = Instant.now()

        val sets = buildList {
            setsByExercise.forEach { (exerciseId, list) ->
                list.forEachIndexed { idx, (reps, weight) ->
                    add(
                        WorkoutSetEntity(
                            id = UUID.randomUUID().toString(),
                            workoutId = workoutId,
                            exerciseId = exerciseId,
                            setIndex = idx + 1,
                            reps = reps,
                            weight = weight,
                            createdAt = now,
                            updatedAt = now,
                            deleted = false
                        )
                    )
                }
            }
        }

        val workout = WorkoutEntity(
            id = workoutId,
            date = date,
            notes = notes,
            createdAt = now,
            updatedAt = now,
            deleted = false
        )

        dao.insertWorkoutWithSets(workout, sets)
    }


    suspend fun replaceWorkout(
        workoutId: String,
        notes: String?,
        setsByExercise: Map<Int, List<Pair<Int, Float>>>
    ) {
        val now = Instant.now()
        val newSets = buildList {
            setsByExercise.forEach { (exerciseId, list) ->
                list.forEachIndexed { idx, (reps, weight) ->
                    add(
                        WorkoutSetEntity(
                            id = UUID.randomUUID().toString(),
                            workoutId = workoutId,
                            exerciseId = exerciseId,
                            setIndex = idx + 1,
                            reps = reps,
                            weight = weight,
                            createdAt = now,
                            updatedAt = now,
                            deleted = false
                        )
                    )
                }
            }
        }
        dao.replaceWorkoutSets(workoutId, notes, newSets, now)
    }

    fun observeRecent(limit: Int = 30): Flow<List<WorkoutWithSets>> =
        dao.observeRecentWorkouts(limit).map { workouts ->
            workouts.map { w ->
                val sets = dao.getSetsForWorkout(w.id)
                WorkoutWithSets(w, sets)
            }
        }

    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<WorkoutWithSets>> =
        dao.observeWorkoutsInRange(from, to).map { workouts ->
            workouts.map { w ->
                val sets = dao.getSetsForWorkout(w.id)
                WorkoutWithSets(w, sets)
            }
        }


    suspend fun detectPRsForBatch(
        setsByExercise: Map<Int, List<Pair<Int, Float>>>
    ): Map<Int, Float> {
        val prs = mutableMapOf<Int, Float>()
        for ((exerciseId, list) in setsByExercise) {
            val newMax = list.maxOfOrNull { it.second } ?: continue
            val prev = dao.getMaxWeightForExercise(exerciseId) ?: Float.NEGATIVE_INFINITY
            if (newMax > prev) prs[exerciseId] = newMax
        }
        return prs
    }
}
