package com.befitnessapp.ui.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.data.local.entity.WorkoutSetEntity
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.ui.localization.LocalStrings
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutLogScreen(onBack: () -> Unit) {
    val vm: WorkoutLogViewModel =
        viewModel(factory = WorkoutLogViewModel.factory(Graph.workoutRepository))
    val items by vm.recent.collectAsState(emptyList())
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val strings = LocalStrings.current.log

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(strings.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        if (items.isEmpty()) {
            Text(
                strings.emptyText,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { w ->
                    Card {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(
                                "${strings.datePrefix} ${w.workout.date.format(fmt)}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (!w.workout.notes.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${strings.notesPrefix} ${w.workout.notes}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            val grouped: Map<Int, List<WorkoutSetEntity>> =
                                w.sets.groupBy { it.exerciseId }
                            val orderedExerciseIds: List<Int> = grouped.keys.sorted()

                            for (exId in orderedExerciseIds) {
                                val list: List<WorkoutSetEntity> = grouped[exId].orEmpty()
                                val name: String = exerciseNameById(exId)

                                Text(name, style = MaterialTheme.typography.bodyMedium)
                                list.sortedBy { it.setIndex }.forEach { s ->
                                    Text(strings.repsWeightFormat(s.reps, s.weight))
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(strings.backButton)
        }
    }
}

private fun exerciseNameById(exId: Int): String {
    val all = Catalogo.searchExercises(query = "", groupId = null)
    return all.firstOrNull { it.id == exId }?.name ?: "Ejercicio $exId"
}
