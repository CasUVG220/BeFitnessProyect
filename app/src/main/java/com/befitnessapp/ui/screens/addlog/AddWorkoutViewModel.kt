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

    /** Reemplaza todos los sets de un ejercicio (se usa cuando editas desde el sheet). */
    fun replaceSets(exerciseId: Int, newSets: List<Pair<Int, Float>>) {
        _entries.value = _entries.value.map { e ->
            if (e.exerciseId == exerciseId) e.copy(sets = newSets.toMutableList()) else e
        }
    }

    fun clearAll() {
        _entries.value = emptyList()
    }

    /**
     * Guarda el workout en base y devuelve los PRs detectados en este batch.
     * - notes: la nota opcional (aquí podemos mandar el nombre de la rutina aplicada).
     * - onDone: recibe un mapa exerciseId -> nuevo peso máximo.
     */
    fun save(
        notes: String?,
        onDone: (prs: Map<Int, Float>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val map = _entries.value
                    .filter { it.sets.isNotEmpty() }
                    .associate { it.exerciseId to it.sets.toList() }

                if (map.isEmpty()) {
                    onDone(emptyMap())
                    return@launch
                }

                // 1) Detectamos PRs comparando contra el historial actual
                val prs = repo.detectPRsForBatch(map)

                // 2) Guardamos el workout con todos los sets
                repo.createWorkout(_date.value, notes, map)

                // 3) Limpiamos el draft en memoria
                _entries.value = emptyList()

                onDone(prs)
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
