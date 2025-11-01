package com.befitnessapp.data.local.routines

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Insert suspend fun insertRoutine(routine: RoutineEntity): Long
    @Update suspend fun updateRoutine(routine: RoutineEntity)

    @Insert suspend fun insertRoutineExercises(exercises: List<RoutineExerciseEntity>): List<Long>
    @Insert suspend fun insertSetTemplates(sets: List<RoutineSetTemplateEntity>)

    @Transaction
    suspend fun upsertRoutineWithContent(data: RoutineWithContent): Long {
        val id = if (data.routine.id == 0L) insertRoutine(data.routine) else {
            updateRoutine(data.routine); data.routine.id
        }
        deleteExercisesByRoutine(id)
        val newExIds = insertRoutineExercises(
            data.exercises.mapIndexed { idx, it ->
                it.exercise.copy(id = 0L, routineId = id, orderIndex = idx)
            }
        )
        val allSets = buildList {
            data.exercises.forEachIndexed { idx, ex ->
                val newId = newExIds[idx]
                addAll(ex.sets.mapIndexed { sIdx, s ->
                    s.copy(id = 0L, routineExerciseId = newId, indexInExercise = sIdx)
                })
            }
        }
        insertSetTemplates(allSets)
        return id
    }

    @Transaction
    @Query("SELECT * FROM routines WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun observeActiveRoutines(): Flow<List<RoutineWithContent>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): RoutineWithContent?

    @Query("UPDATE routines SET isArchived = 1 WHERE id = :id")
    suspend fun archiveRoutine(id: Long)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteExercisesByRoutine(routineId: Long)
}
