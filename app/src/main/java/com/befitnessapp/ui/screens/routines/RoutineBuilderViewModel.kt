package com.befitnessapp.ui.screens.routines

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.befitnessapp.domain.routines.RoutineDetail

/**
 * ViewModel del Builder de Rutinas.
 *
 * - En CREACIÓN/EDICIÓN la UI solo muestra: nombre, ejercicios y # de sets por ejercicio.
 * - Las reps/peso no se editan aquí, pero se PRESERVAN si la rutina ya las tenía.
 * - Cuando la rutina se usa en AddWorkout, ahí sí trabajas los valores por set.
 */

data class RoutineItem(
    val exerciseId: Int,
    /** Lista de sets: Pair(reps, weight). En el builder solo contamos tamaño, pero guardamos lo que venga. */
    val sets: MutableList<Pair<Int, Float>> = mutableListOf()
)

data class RoutineBuilderUi(
    val routineId: Long? = null,
    val name: String = "",
    val exercises: List<RoutineItem> = emptyList()
)

class RoutineBuilderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineBuilderUi())
    val uiState: StateFlow<RoutineBuilderUi> = _uiState.asStateFlow()

    /* ---------- Mutadores simples ---------- */

    fun setName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun setRoutineId(id: Long?) {
        _uiState.value = _uiState.value.copy(routineId = id)
    }

    fun setExercises(items: List<RoutineItem>) {
        _uiState.value = _uiState.value.copy(exercises = items)
    }

    /* ---------- Carga para EDICIÓN (precarga real, sin “rehacer”) ---------- */

    /**
     * Precarga desde un RoutineDetail existente:
     * - Copia nombre e id.
     * - Copia ejercicios.
     * - Copia TODOS los sets existentes (reps/weight) para no perderlos.
     *   La UI del builder solo manipula la cantidad, pero mantenemos los valores.
     */
    fun loadFrom(detail: RoutineDetail) {
        val items = detail.exercises.map { ex ->
            val count = ex.sets.size.coerceAtLeast(1)
            val sets = MutableList(count) { idx ->
                val s = ex.sets.getOrNull(idx)
                // Si no hay dato para ese índice, usamos un placeholder razonable
                (s?.reps ?: 10) to (s?.weight ?: 0f)
            }
            RoutineItem(
                exerciseId = ex.exerciseId,
                sets = sets
            )
        }

        _uiState.value = _uiState.value.copy(
            routineId = detail.routine.id,
            name = detail.routine.name,
            exercises = items
        )
    }

    /* ---------- Edición en memoria ---------- */

    fun addExercise(exId: Int) {
        val st = _uiState.value
        if (st.exercises.any { it.exerciseId == exId }) return
        _uiState.value = st.copy(
            exercises = st.exercises + RoutineItem(
                exerciseId = exId,
                // mínimo 1 set al crear
                sets = mutableListOf(10 to 0f)
            )
        )
    }

    fun removeExercise(exId: Int) {
        val st = _uiState.value
        _uiState.value = st.copy(exercises = st.exercises.filterNot { it.exerciseId == exId })
    }

    /**
     * Cambia solo la CANTIDAD de sets de un ejercicio (se preservan reps/weight cuando se pueda).
     * - Si se aumentan sets: se agregan con placeholders (10 reps, 0f weight).
     * - Si se reducen: se recorta la lista (compatible minSdk 26 con removeAt).
     */
    fun changeSetsCount(exId: Int, newCount: Int) {
        if (newCount < 1) return
        val st = _uiState.value
        _uiState.value = st.copy(
            exercises = st.exercises.map { item ->
                if (item.exerciseId != exId) item else {
                    val updated = item.sets.toMutableList()
                    while (updated.size < newCount) {
                        updated.add(10 to 0f)
                    }
                    while (updated.size > newCount && updated.isNotEmpty()) {
                        updated.removeAt(updated.lastIndex) // API 26-friendly
                    }
                    item.copy(sets = updated)
                }
            }
        )
    }

    /**
     * Reemplaza por completo los sets de un ejercicio (por si alguna UI futura requiere sobrescribirlos).
     * Útil si más adelante agregas un “modo avanzado” con edición de reps/peso.
     */
    fun replaceSets(exId: Int, newSets: List<Pair<Int, Float>>) {
        val st = _uiState.value
        _uiState.value = st.copy(
            exercises = st.exercises.map { item ->
                if (item.exerciseId != exId) item else item.copy(sets = newSets.toMutableList())
            }
        )
    }

    /* ---------- Guardado ---------- */

    /**
     * Guarda la rutina usando un saver provisto por la pantalla.
     * Firma compatible con tu pantalla actual (name, items).
     */
    fun saveRoutine(
        saver: suspend (name: String, items: List<RoutineItem>) -> Unit,
        onSaved: () -> Unit
    ) {
        val ui = _uiState.value
        val valid = ui.name.isNotBlank() && ui.exercises.any { it.sets.isNotEmpty() }
        if (!valid) return

        CoroutineScope(Dispatchers.IO).launch {
            saver(ui.name.trim(), ui.exercises)
            onSaved()
        }
    }
}
