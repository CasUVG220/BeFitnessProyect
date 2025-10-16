package com.befitnessapp.ui.screens.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.befitnessapp.data.repository.WorkoutRepository
import com.befitnessapp.domain.recommendation.ExerciseScore
import com.befitnessapp.domain.recommendation.defaultWeeklyTargets
import com.befitnessapp.domain.recommendation.rankExercises
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class RecommendationsViewModel(private val repo: WorkoutRepository) : ViewModel() {

    private val windowDays = 14L
    private val targets = defaultWeeklyTargets()

    val suggestions: StateFlow<List<ExerciseScore>> =
        repo.observeRange(
            from = LocalDate.now().minusDays(windowDays),
            to   = LocalDate.now()
        )
            .map { history -> rankExercises(history, targets, topN = 6) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun factory(repo: WorkoutRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecommendationsViewModel(repo) as T
            }
        }
    }
}
