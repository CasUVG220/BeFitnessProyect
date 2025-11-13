package com.befitnessapp.ui.screens.addlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.befitnessapp.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExerciseEntry(
    val exerciseId: Int,
    val sets: MutableList<Pair<Int, Float>> = mutableListOf() // (reps, weight)
)

class AddWorkoutViewModel(
    private val repo: WorkoutRepository
) : ViewModel() {

    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date
    fun setDate(d: LocalDate) { _date.value = d }

    private val _entries = MutableStateFlow<List<ExerciseEntry>>(emptyList())
    val entries: StateFlow<List<ExerciseEntry>> = _entries

    fun addExercise(exerciseId: Int) {
        if (_entries.value.any { it.exerciseId == exerciseId }) return
        _entries.value = _entries.value + ExerciseEntry(exerciseId)
    }

    fun removeExercise(exerciseId: Int) {
        _entries.value = _entries.value.filterNot { it.exerciseId == exerciseId }
    }

    fun addSet(exerciseId: Int, reps: Int = 8, weight: Float = 20f) {
        _entries.value = _entries.value.map {
            if (it.exerciseId == exerciseId) {
                it.sets.add(reps to weight); it
            } else it
        }
    }

    fun updateSet(exerciseId: Int, index: Int, reps: Int, weight: Float) {
        _entries.value = _entries.value.map {
            if (it.exerciseId == exerciseId && index in it.sets.indices) {
                it.sets[index] = reps to weight; it
            } else it
        }
    }

    fun removeSet(exerciseId: Int, index: Int) {
        _entries.value = _entries.value.map {
            if (it.exerciseId == exerciseId && index in it.sets.indices) {
                it.sets.removeAt(index); it
            } else it
        }
    }

    fun replaceSets(exerciseId: Int, newSets: List<Pair<Int, Float>>) {
        _entries.value = _entries.value.map { e ->
            if (e.exerciseId == exerciseId) e.copy(sets = newSets.toMutableList()) else e
        }
    }

    fun clearAll() {
        _entries.value = emptyList()
    }

    fun save(notes: String?, onDone: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                val map = _entries.value
                    .filter { it.sets.isNotEmpty() }
                    .associate { it.exerciseId to it.sets.toList() }
                repo.createWorkout(_date.value, notes, map)
                _entries.value = emptyList()
                onDone()
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    companion object {
        fun factory(repo: WorkoutRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddWorkoutViewModel(repo) as T
            }
        }
    }
}
