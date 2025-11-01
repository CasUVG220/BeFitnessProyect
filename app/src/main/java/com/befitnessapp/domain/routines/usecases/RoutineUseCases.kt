package com.befitnessapp.domain.routines.usecases

import com.befitnessapp.data.repository.RoutineRepository
import com.befitnessapp.domain.routines.RoutineDetail
import kotlinx.coroutines.flow.Flow

class ObserveRoutinesUC(private val repo: RoutineRepository) {
    operator fun invoke(): Flow<List<RoutineDetail>> = repo.observeRoutines()
}
class GetRoutineUC(private val repo: RoutineRepository) {
    suspend operator fun invoke(id: Long): RoutineDetail? = repo.getRoutine(id)
}
class SaveRoutineUC(private val repo: RoutineRepository) {
    suspend operator fun invoke(detail: RoutineDetail): Long = repo.upsert(detail)
}
class ArchiveRoutineUC(private val repo: RoutineRepository) {
    suspend operator fun invoke(id: Long) = repo.archive(id)
}
