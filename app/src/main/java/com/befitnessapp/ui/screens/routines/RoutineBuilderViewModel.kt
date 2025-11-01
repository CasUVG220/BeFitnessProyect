package com.befitnessapp.ui.screens.routines

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * VM para construir/editar una rutina EN MEMORIA.
 * Guarda: nombre, ejercicios y sets (solo reps se persistirán).
 * Los pesos NO se guardan en la plantilla.
 */

data class RoutineItem(
    val exerciseId: Int,
    val sets: MutableList<Pair<Int, Float>> = mutableListOf() // (reps, weight) – el weight NO se persiste
)

data class RoutineBuilderUi(
    val routineId: Long? = null,
    val name: String = "",
    val exercises: List<RoutineItem> = emptyList()
)

class RoutineBuilderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineBuilderUi())
    val uiState: StateFlow<RoutineBuilderUi> = _uiState.asStateFlow()

    fun setName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun addExercise(exId: Int) {
        val st = _uiState.value
        if (st.exercises.any { it.exerciseId == exId }) return
        _uiState.value = st.copy(exercises = st.exercises + RoutineItem(exId))
    }

    fun removeExercise(exId: Int) {
        val st = _uiState.value
        _uiState.value = st.copy(exercises = st.exercises.filterNot { it.exerciseId == exId })
    }

    /** Reemplaza todos los sets del ejercicio. */
    fun replaceSets(exId: Int, newSets: List<Pair<Int, Float>>) {
        val st = _uiState.value
        _uiState.value = st.copy(
            exercises = st.exercises.map {
                if (it.exerciseId == exId) it.copy(sets = newSets.toMutableList()) else it
            }
        )
    }

    /**
     * Guarda la rutina usando un "saver" provisto por la UI (para inyectar el repo real).
     * El "saver" DEBE persistir solo reps por set (los pesos se ignoran).
     */
    fun saveRoutine(
        saver: suspend (name: String, items: List<RoutineItem>) -> Unit,
        onSaved: () -> Unit
    ) {
        val ui = _uiState.value
        val valid = ui.name.isNotBlank() && ui.exercises.any { it.sets.isNotEmpty() }
        if (!valid) return

        CoroutineScope(Dispatchers.IO).launch {
            saver(ui.name, ui.exercises)
            onSaved()
        }
    }
}
