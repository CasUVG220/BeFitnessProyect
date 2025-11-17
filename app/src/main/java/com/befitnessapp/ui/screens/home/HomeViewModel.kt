package com.befitnessapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.befitnessapp.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeWorkoutUi(
    val id: String,
    val title: String,
    val date: LocalDate,
    val volume: Float,
    val sets: Int,
    val reps: Int
)

data class WeeklySummaryUi(
    val volume: Float = 0f,
    val reps: Int = 0,
    val sets: Int = 0,
    val prs: Int = 0
)

data class HomeUiState(
    val today: LocalDate = LocalDate.now(),
    val weekly: WeeklySummaryUi = WeeklySummaryUi(),
    val lastWorkouts: List<HomeWorkoutUi> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repo: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeWeek()
    }

    private fun observeWeek() {
        val today = LocalDate.now()
        val from = today.minusDays(6)

        viewModelScope.launch {
            repo.observeRange(from, today).collect { workouts ->
                val items = workouts
                    .sortedByDescending { it.workout.date }
                    .map { ww ->
                        val volume = ww.sets.sumOf { (it.reps * it.weight).toDouble() }.toFloat()
                        val reps = ww.sets.sumOf { it.reps }
                        val sets = ww.sets.size
                        val title = ww.workout.notes?.takeIf { it.isNotBlank() } ?: "Entrenamiento"
                        HomeWorkoutUi(
                            id = ww.workout.id,
                            title = title,
                            date = ww.workout.date,
                            volume = volume,
                            sets = sets,
                            reps = reps
                        )
                    }

                val weeklyVolume = items.sumOf { it.volume.toDouble() }.toFloat()
                val weeklyReps = items.sumOf { it.reps }
                val weeklySets = items.sumOf { it.sets }

                val allSets = workouts.flatMap { it.sets }
                val weeklyPrs = allSets
                    .groupBy { it.exerciseId }
                    .values
                    .count { sets ->
                        sets.maxOfOrNull { it.weight }?.let { it > 0f } == true
                    }

                _uiState.value = HomeUiState(
                    today = today,
                    weekly = WeeklySummaryUi(
                        volume = weeklyVolume,
                        reps = weeklyReps,
                        sets = weeklySets,
                        prs = weeklyPrs
                    ),
                    lastWorkouts = items.take(5),
                    isLoading = false
                )
            }
        }
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
