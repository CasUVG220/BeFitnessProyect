package com.befitnessapp.ui.screens.addlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.befitnessapp.data.repository.WorkoutRepository
import com.befitnessapp.domain.catalog.Catalogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExerciseEntry(
    val exerciseId: Int,
    val sets: MutableList<Pair<Int, Float>> = mutableListOf() // (reps, weight)
)

data class PrItem(
    val exerciseId: Int,
    val exerciseName: String,
    val metricLabel: String,   // p.ej. "Peso máx"
    val displayValue: String   // p.ej. "120.0"
)

class AddWorkoutViewModel(
    private val repo: WorkoutRepository
) : ViewModel() {

    private val _entries = MutableStateFlow<List<ExerciseEntry>>(emptyList())
    val entries: StateFlow<List<ExerciseEntry>> = _entries

    val sessionPrs = MutableStateFlow<List<PrItem>>(emptyList())

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


    suspend fun detectAndSetSessionPrs(): List<PrItem> {
        val pending = _entries.value
            .filter { it.sets.isNotEmpty() }
            .associate { it.exerciseId to it.sets.toList() }

        if (pending.isEmpty()) {
            sessionPrs.value = emptyList()
            return emptyList()
        }

        val maxByExercise: Map<Int, Float> = repo.detectPRsForBatch(pending)
        if (maxByExercise.isEmpty()) {
            sessionPrs.value = emptyList()
            return emptyList()
        }

        val all = Catalogo.searchExercises(query = "", groupId = null)
        val prs = maxByExercise.map { (exId, w) ->
            val name = all.firstOrNull { it.id == exId }?.name ?: "Ejercicio $exId"
            PrItem(
                exerciseId = exId,
                exerciseName = name,
                metricLabel = "Peso máx",
                displayValue = formatOneDecimal(w)
            )
        }.sortedBy { it.exerciseName }

        sessionPrs.value = prs
        return prs
    }

    fun saveToday(notes: String?, onDone: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                val map = _entries.value
                    .filter { it.sets.isNotEmpty() }
                    .associate { it.exerciseId to it.sets.toList() }
                repo.createWorkout(LocalDate.now(), notes, map)
                _entries.value = emptyList()
                onDone()
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    private fun formatOneDecimal(value: Float): String {
        val s = String.format("%.1f", value)
        return if (s.endsWith(".0")) s.dropLast(2) else s
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
