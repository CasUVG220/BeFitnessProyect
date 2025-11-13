package com.befitnessapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.befitnessapp.data.local.dao.WorkoutWithSets
import com.befitnessapp.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val today: LocalDate = LocalDate.now(),
    val weekVolume: Float = 0f,
    val weekReps: Int = 0,
    val weekSets: Int = 0,
    val weekPRs: Int = 0,
    val recent: List<UiWorkout> = emptyList()
)

data class UiWorkout(
    val title: String,          // ← NUEVO
    val date: LocalDate,
    val volume: Float,
    val sets: Int,
    val reps: Int
)

class HomeViewModel(
    private val repo: WorkoutRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _ui

    init {
        viewModelScope.launch {
            val today = LocalDate.now()
            val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())

            val weekAggFlow = repo.observeAggBetween(weekStart, today).map { list ->
                val vol = list.sumOf { it.volume.toDouble() }.toFloat()
                val reps = list.sumOf { it.reps }
                val sets = list.sumOf { it.sets }
                Triple(vol, reps, sets)
            }

            val recentFlow = repo.observeRecent(limit = 10)
                .map { ww -> ww.map { toUiWorkout(it) }.sortedByDescending { it.date }.take(3) }

            val weekWorkoutsFlow = repo.observeRange(weekStart, today)

            combine(recentFlow, weekAggFlow, weekWorkoutsFlow) { recent, agg, weekW ->
                Triple(recent, agg, weekW)
            }.collectLatest { (recent, agg, weekW) ->
                val (vol, reps, sets) = agg

                val setsByExercise = buildMap<Int, MutableList<Pair<Int, Float>>> {
                    weekW.forEach { w -> w.sets.forEach { s ->
                        getOrPut(s.exerciseId) { mutableListOf() }.add(s.reps to s.weight)
                    } }
                }
                val prs = repo.detectPRsForBatch(setsByExercise).size

                _ui.value = _ui.value.copy(
                    today = today,
                    weekVolume = vol,
                    weekReps = reps,
                    weekSets = sets,
                    weekPRs = prs,
                    recent = recent
                )
            }
        }
    }

    private fun toUiWorkout(w: WorkoutWithSets): UiWorkout {
        val vol = w.sets.sumOf { (it.reps * it.weight).toDouble() }.toFloat()
        val reps = w.sets.sumOf { it.reps }
        val sets = w.sets.size
        val title = w.workout.notes?.takeIf { it.isNotBlank() } ?: "Entrenamiento"
        return UiWorkout(
            title = title,
            date = w.workout.date,
            volume = vol,
            sets = sets,
            reps = reps
        )
    }

    companion object {
        fun factory(repo: WorkoutRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(repo) as T
            }
        }
    }
}
