package com.befitnessapp.routines

import android.content.Context
import android.content.SharedPreferences
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.routines.Routine
import com.befitnessapp.domain.routines.RoutineDetail
import com.befitnessapp.domain.routines.RoutineExerciseTemplate
import com.befitnessapp.domain.routines.RoutineSetTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repositorio simple basado en SharedPreferences para Rutinas (plantillas).
 * Se usa en la UI del builder/listado. NO interfiere con tu Room.
 */
object RoutinesServiceLocator {

    private const val PREFS = "befitness_routines"
    private const val KEY_DATA = "routines_v1"

    interface RoutinesRepository {
        fun observeRoutines(): Flow<List<RoutineDetail>>
        suspend fun getRoutine(id: Long): RoutineDetail?
        suspend fun saveRoutine(name: String, items: List<Pair<Int, Int>>): String     // crea y devuelve id (String)
        suspend fun updateRoutine(id: Long, name: String, items: List<Pair<Int, Int>>) // edita por id
        suspend fun deleteRoutine(id: Long)                                            // elimina por id
    }

    fun repository(ctx: Context): RoutinesRepository =
        RepositoryImpl(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE))

    // -------------------- Impl interna --------------------
    private class RepositoryImpl(
        private val prefs: SharedPreferences
    ) : RoutinesRepository {

        private val cache = MutableStateFlow(loadFromPrefs())

        override fun observeRoutines(): Flow<List<RoutineDetail>> = cache.asStateFlow()

        override suspend fun getRoutine(id: Long): RoutineDetail? =
            cache.value.firstOrNull { it.routine.id == id }

        override suspend fun saveRoutine(name: String, items: List<Pair<Int, Int>>): String {
            val idStr = System.currentTimeMillis().toString()

            val exercises = items.map { (exId, setsCountRaw) ->
                val setsCount = setsCountRaw.coerceAtLeast(1)
                val display = Catalogo.allExercises.firstOrNull { it.id == exId }?.name
                    ?: "Ejercicio $exId"
                RoutineExerciseTemplate(
                    exerciseId = exId,
                    displayName = display,
                    sets = List(setsCount) { RoutineSetTemplate(reps = 10, weight = 0f) }
                )
            }

            val newDetail = RoutineDetail(
                routine = Routine(id = idStr.toLongOrNull() ?: 0L, name = name),
                exercises = exercises
            )

            val updated = cache.value + newDetail
            cache.value = updated
            saveToPrefs(updated)
            return idStr
        }

        override suspend fun updateRoutine(id: Long, name: String, items: List<Pair<Int, Int>>) {
            val current = cache.value.toMutableList()
            val idx = current.indexOfFirst { it.routine.id == id }
            if (idx == -1) return

            val exercises = items.map { (exId, setsCountRaw) ->
                val setsCount = setsCountRaw.coerceAtLeast(1)
                val display = Catalogo.allExercises.firstOrNull { it.id == exId }?.name
                    ?: "Ejercicio $exId"
                RoutineExerciseTemplate(
                    exerciseId = exId,
                    displayName = display,
                    sets = List(setsCount) { RoutineSetTemplate(reps = 10, weight = 0f) }
                )
            }

            current[idx] = current[idx].copy(
                routine = current[idx].routine.copy(name = name),
                exercises = exercises
            )
            cache.value = current
            saveToPrefs(current)
        }

        override suspend fun deleteRoutine(id: Long) {
            val updated = cache.value.filterNot { it.routine.id == id }
            cache.value = updated
            saveToPrefs(updated)
        }

        // -------------------- Persistencia JSON --------------------
        private fun loadFromPrefs(): List<RoutineDetail> {
            val raw = prefs.getString(KEY_DATA, null) ?: return emptyList()
            return try {
                val arr = JSONArray(raw)
                (0 until arr.length()).mapNotNull { i ->
                    val o = arr.getJSONObject(i)
                    val id = o.optString("id", System.nanoTime().toString())
                    val name = o.optString("name", "Rutina")
                    val exArr = o.optJSONArray("items") ?: JSONArray()
                    val items = (0 until exArr.length()).map { j ->
                        val ej = exArr.getJSONObject(j)
                        val exId = ej.getInt("exerciseId")
                        val display = ej.optString("display", Catalogo.allExercises.firstOrNull { it.id == exId }?.name ?: "Ejercicio $exId")
                        val sets = ej.optInt("sets", 1).coerceAtLeast(1)
                        RoutineExerciseTemplate(
                            exerciseId = exId,
                            displayName = display,
                            sets = List(sets) { RoutineSetTemplate(reps = 10, weight = 0f) }
                        )
                    }
                    RoutineDetail(routine = Routine(id = id.toLongOrNull() ?: 0L, name = name), exercises = items)
                }
            } catch (_: Throwable) {
                emptyList()
            }
        }

        private fun saveToPrefs(list: List<RoutineDetail>) {
            val arr = JSONArray()
            list.forEach { detail ->
                val o = JSONObject()
                val r = detail.routine
                o.put("id", if (r.id == 0L) System.nanoTime().toString() else r.id.toString())
                o.put("name", r.name)
                val exArr = JSONArray()
                detail.exercises.forEach { ex ->
                    val e = JSONObject()
                    e.put("exerciseId", ex.exerciseId)
                    e.put("display", ex.displayName)
                    e.put("sets", ex.sets.size)
                    exArr.put(e)
                }
                o.put("items", exArr)
                arr.put(o)
            }
            prefs.edit().putString(KEY_DATA, arr.toString()).apply()
        }
    }
}
