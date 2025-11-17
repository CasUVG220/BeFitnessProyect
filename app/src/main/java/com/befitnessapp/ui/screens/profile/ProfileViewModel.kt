package com.befitnessapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.befitnessapp.data.local.dao.WorkoutWithSets
import com.befitnessapp.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ProfileUiState(
    val isLoading: Boolean = true,
    val totalWorkouts: Int = 0,
    val totalVolume: Float = 0f,
    val totalSets: Int = 0,
    val totalReps: Int = 0,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
    val firstWorkoutDate: LocalDate? = null
)

class ProfileViewModel(
    private val repo: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val today = LocalDate.now()
            val from = today.minusYears(1)

            combine(
                repo.observeRecent(limit = 10_000),
                repo.observeDaysWithLogsBetween(from, today)
            ) { workouts, days ->
                buildProfileState(workouts, days)
            }.collect { state ->
                _uiState.value = state.copy(isLoading = false)
            }
        }
    }

    companion object {
        fun factory(repo: WorkoutRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(repo) as T
            }
        }
    }
}

private fun buildProfileState(
    workouts: List<WorkoutWithSets>,
    daysWithLogs: List<LocalDate>
): ProfileUiState {
    if (workouts.isEmpty()) {
        val (best, current) = computeStreaks(daysWithLogs)
        return ProfileUiState(
            isLoading = false,
            bestStreak = best,
            currentStreak = current
        )
    }

    var totalVolume = 0f
    var totalSets = 0
    var totalReps = 0
    var firstDate: LocalDate? = null

    for (w in workouts) {
        val d = w.workout.date
        if (firstDate == null || d.isBefore(firstDate)) {
            firstDate = d
        }
        for (s in w.sets) {
            val reps = s.reps.coerceAtLeast(0)
            val weight = if (s.weight.isFinite()) s.weight else 0f
            totalVolume += reps * weight
            totalReps += reps
            totalSets += 1
        }
    }

    val (best, current) = computeStreaks(daysWithLogs)

    return ProfileUiState(
        isLoading = false,
        totalWorkouts = workouts.size,
        totalVolume = totalVolume,
        totalSets = totalSets,
        totalReps = totalReps,
        bestStreak = best,
        currentStreak = current,
        firstWorkoutDate = firstDate
    )
}

private fun computeStreaks(days: List<LocalDate>): Pair<Int, Int> {
    if (days.isEmpty()) return 0 to 0

    val sorted = days.distinct().sorted()

    var best = 1
    var current = 1

    for (i in 1 until sorted.size) {
        val prev = sorted[i - 1]
        val d = sorted[i]
        if (d == prev.plusDays(1)) {
            current += 1
        } else if (d != prev) {
            if (current > best) best = current
            current = 1
        }
    }
    if (current > best) best = current

    val today = LocalDate.now()
    val set = sorted.toSet()
    var cur = 0
    var cursor = today
    while (set.contains(cursor)) {
        cur += 1
        cursor = cursor.minusDays(1)
    }

    return best to cur
}
