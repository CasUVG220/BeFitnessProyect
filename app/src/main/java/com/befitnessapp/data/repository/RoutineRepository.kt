package com.befitnessapp.data.repository

import com.befitnessapp.data.local.routines.*
import com.befitnessapp.domain.routines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

interface RoutineRepository {
    fun observeRoutines(): Flow<List<RoutineDetail>>
    suspend fun getRoutine(id: Long): RoutineDetail?
    suspend fun upsert(detail: RoutineDetail): Long
    suspend fun archive(id: Long)
}

class RoutineRepositoryImpl(
    private val dao: RoutineDao
) : RoutineRepository {

    override fun observeRoutines(): Flow<List<RoutineDetail>> =
        dao.observeActiveRoutines().map { list -> list.map { it.toDomain() } }

    override suspend fun getRoutine(id: Long): RoutineDetail? =
        dao.getRoutineById(id)?.toDomain()

    override suspend fun upsert(detail: RoutineDetail): Long {
        val now = Instant.now().toEpochMilli()
        val routine = RoutineEntity(
            id = detail.routine.id,
            name = detail.routine.name,
            createdAt = now,
            updatedAt = now,
            isArchived = detail.routine.isArchived
        )
        val content = RoutineWithContent(
            routine = routine,
            exercises = detail.exercises.map { ex ->
                RoutineExerciseWithSets(
                    exercise = RoutineExerciseEntity(
                        id = 0L,
                        routineId = 0L,
                        exerciseId = ex.exerciseId,
                        displayName = ex.displayName,
                        orderIndex = 0
                    ),
                    sets = ex.sets.mapIndexed { idx, s ->
                        RoutineSetTemplateEntity(
                            id = 0L,
                            routineExerciseId = 0L,
                            indexInExercise = idx,
                            reps = s.reps,
                            weight = s.weight
                        )
                    }
                )
            }
        )
        return dao.upsertRoutineWithContent(content)
    }

    override suspend fun archive(id: Long) {
        dao.archiveRoutine(id)
    }
}

private fun RoutineWithContent.toDomain(): RoutineDetail =
    RoutineDetail(
        routine = Routine(
            id = routine.id,
            name = routine.name,
            isArchived = routine.isArchived
        ),
        exercises = exercises.sortedBy { it.exercise.orderIndex }.map { ex ->
            RoutineExerciseTemplate(
                exerciseId = ex.exercise.exerciseId,
                displayName = ex.exercise.displayName,
                sets = ex.sets.sortedBy { it.indexInExercise }.map { s ->
                    RoutineSetTemplate(reps = s.reps, weight = s.weight)
                }
            )
        }
    )
