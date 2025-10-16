package com.befitnessapp.ui.screens.musclemap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.befitnessapp.data.repository.WorkoutRepository
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.recommendation.computeCoverage
import com.befitnessapp.domain.recommendation.defaultWeeklyTargets
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class MuscleMapViewModel(private val repo: WorkoutRepository) : ViewModel() {

    private val windowDays = 14L
    private val targets = defaultWeeklyTargets()


    val coverageByCanonical: StateFlow<Map<Int, Float>> =
        repo.observeRange(
            from = LocalDate.now().minusDays(windowDays),
            to   = LocalDate.now()
        ).map { history ->
            computeCoverage(history, targets).coverageByCanonical
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    //Lista de músculos canónicos ordenados por grupo

    val canonicalMuscles = Catalogo.allMuscles.filter { it.parentId == null }
        .sortedWith(compareBy({ it.groupId }, { it.name }))

    companion object {
        fun factory(repo: WorkoutRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MuscleMapViewModel(repo) as T
            }
        }
    }
}
