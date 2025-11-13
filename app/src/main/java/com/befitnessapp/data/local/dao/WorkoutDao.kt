package com.befitnessapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.befitnessapp.data.local.entity.WorkoutEntity
import com.befitnessapp.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

data class WorkoutWithSets(
    val workout: WorkoutEntity,
    val sets: List<WorkoutSetEntity>
)

// agregarcion por ejercicios
data class ExerciseAgg(
    val exerciseId: Int,
    val sets: Int,
    val reps: Int,
    val volume: Float,
    val lastDate: LocalDate?
)

@Dao
interface WorkoutDao {

    @Upsert
    suspend fun upsertWorkout(workout: WorkoutEntity)

    @Upsert
    suspend fun upsertSets(sets: List<WorkoutSetEntity>)

    @Transaction
    suspend fun insertWorkoutWithSets(workout: WorkoutEntity, sets: List<WorkoutSetEntity>) {
        upsertWorkout(workout)
        if (sets.isNotEmpty()) upsertSets(sets)
    }

    @Query("UPDATE workout SET notes = :notes, updatedAt = :updatedAt WHERE id = :workoutId")
    suspend fun updateWorkoutNotes(workoutId: String, notes: String?, updatedAt: Instant)

    @Query("DELETE FROM workout_set WHERE workoutId = :workoutId")
    suspend fun deleteSetsByWorkoutId(workoutId: String)

    @Transaction
    suspend fun replaceWorkoutSets(
        workoutId: String,
        notes: String?,
        newSets: List<WorkoutSetEntity>,
        now: Instant = Instant.now()
    ) {
        updateWorkoutNotes(workoutId, notes, now)
        deleteSetsByWorkoutId(workoutId)
        if (newSets.isNotEmpty()) upsertSets(newSets)
    }

    @Query(
        """
        SELECT * FROM workout 
        WHERE deleted = 0 
        ORDER BY date DESC, createdAt DESC 
        LIMIT :limit
        """
    )
    fun observeRecentWorkouts(limit: Int = 50): Flow<List<WorkoutEntity>>

    @Query(
        """
        SELECT * FROM workout 
        WHERE deleted = 0 AND date BETWEEN :from AND :to
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun observeWorkoutsInRange(from: LocalDate, to: LocalDate): Flow<List<WorkoutEntity>>

    @Query(
        """
        SELECT * FROM workout_set 
        WHERE deleted = 0 AND workoutId = :workoutId
        ORDER BY exerciseId ASC, setIndex ASC
        """
    )
    suspend fun getSetsForWorkout(workoutId: String): List<WorkoutSetEntity>

    @Query(
        """
        SELECT MAX(weight) FROM workout_set 
        WHERE deleted = 0 AND exerciseId = :exerciseId
        """
    )
    suspend fun getMaxWeightForExercise(exerciseId: Int): Float?

    //NUEVO: Agregaciones para el algoritmo

    @Query(
        """
        SELECT 
            ws.exerciseId            AS exerciseId,
            COUNT(*)                 AS sets,
            COALESCE(SUM(ws.reps),0)                      AS reps,
            COALESCE(SUM(ws.reps * ws.weight),0)          AS volume,
            MAX(w.date)              AS lastDate
        FROM workout_set ws
        JOIN workout w ON w.id = ws.workoutId
        WHERE ws.deleted = 0
          AND w.deleted  = 0
          AND w.date BETWEEN :from AND :to
        GROUP BY ws.exerciseId
        ORDER BY ws.exerciseId ASC
        """
    )
    fun observeAggBetween(from: LocalDate, to: LocalDate): Flow<List<ExerciseAgg>>

    @Query(
        """
        SELECT DISTINCT w.date
        FROM workout w
        WHERE w.deleted = 0 AND w.date BETWEEN :from AND :to
        ORDER BY w.date ASC
        """
    )
    fun observeDaysWithLogsBetween(from: LocalDate, to: LocalDate): Flow<List<LocalDate>>
    // 🔥 PRs: cuántos ejercicios tienen su peso MÁXIMO global
    // en un workout dentro del rango [from, to]
    @Query(
        """
        SELECT COUNT(DISTINCT ws.exerciseId) AS value
        FROM workout_set ws
        JOIN workout w ON w.id = ws.workoutId
        JOIN (
            SELECT exerciseId, MAX(weight) AS maxWeight
            FROM workout_set
            WHERE deleted = 0
            GROUP BY exerciseId
        ) best
          ON best.exerciseId = ws.exerciseId
         AND ws.weight = best.maxWeight
        WHERE ws.deleted = 0
          AND w.deleted  = 0
          AND w.date BETWEEN :from AND :to
        """
    )
    fun observePrsBetween(from: LocalDate, to: LocalDate): Flow<Int>

}

