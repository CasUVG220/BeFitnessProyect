package com.befitnessapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.befitnessapp.data.local.dao.ExerciseAgg
import com.befitnessapp.data.local.dao.WorkoutWithSets
import com.befitnessapp.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeWorkout(
    val id: String,
    val title: String,
    val date: LocalDate,
    val volume: Float,
    val sets: Int,
    val reps: Int
)

data class HomeUiState(
    val today: LocalDate = LocalDate.now(),
    val weeklyVolume: Float = 0f,
    val weeklyReps: Int = 0,
    val weeklySets: Int = 0,
    val weeklyPrs: Int = 0,
    val lastWorkout: HomeWorkout? = null,
    val recentWorkouts: List<HomeWorkout> = emptyList()
)

class HomeViewModel(
    private val repo: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        val today = LocalDate.now()
        val from = today.minusDays(6)

        viewModelScope.launch {
            repo.observeAggBetween(from, today).collectLatest { aggs ->
                val weeklyVolume = aggs.sumOf { it.volume.toDouble() }.toFloat()
                val weeklyReps = aggs.sumOf { it.reps }
                val weeklySets = aggs.sumOf { it.sets }

                _uiState.value = _uiState.value.copy(
                    today = today,
                    weeklyVolume = weeklyVolume,
                    weeklyReps = weeklyReps,
                    weeklySets = weeklySets
                )
            }
        }

        viewModelScope.launch {
            repo.observePrsBetween(from, today).collectLatest { prsCount ->
                _uiState.value = _uiState.value.copy(
                    weeklyPrs = prsCount
                )
            }
        }

        viewModelScope.launch {
            repo.observeRecent(limit = 10).collectLatest { list ->
                val uiList = list.map { toHomeWorkout(it) }
                _uiState.value = _uiState.value.copy(
                    lastWorkout = uiList.firstOrNull(),
                    recentWorkouts = uiList
                )
            }
        }
    }

    private fun toHomeWorkout(w: WorkoutWithSets): HomeWorkout {
        val totalVolume = w.sets.sumOf { (it.reps * it.weight).toDouble() }.toFloat()
        val totalReps = w.sets.sumOf { it.reps }
        val totalSets = w.sets.size
        val title = w.workout.notes?.takeIf { it.isNotBlank() } ?: "Entrenamiento"

        return HomeWorkout(
            id = w.workout.id,
            title = title,
            date = w.workout.date,
            volume = totalVolume,
            sets = totalSets,
            reps = totalReps
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
