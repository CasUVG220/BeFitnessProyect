package com.befitnessapp.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.catalog.Exercise
import com.befitnessapp.domain.catalog.MuscleGroup

@Composable
fun LibraryScreen(onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedGroup: MuscleGroup? by remember { mutableStateOf(null) }

    val results by remember(query, selectedGroup) {
        mutableStateOf(
            Catalogo.searchExercises(
                query = query,
                groupId = selectedGroup?.id
            )
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Biblioteca de ejercicios", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar ejercicio") }
        )

        Spacer(Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { selectedGroup = null },
                label = { Text("Todos") }
            )
            Catalogo.allGroups.forEach { grp ->
                val selected = selectedGroup?.id == grp.id
                AssistChip(
                    onClick = { selectedGroup = if (selected) null else grp },
                    label = { Text(grp.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        else
                            MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { ex ->
                ExerciseCard(ex)
            }
            if (results.isEmpty()) {
                item {
                    Text(
                        text = "No se encontraron ejercicios.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun ExerciseCard(ex: Exercise) {
    val primaryTargets = remember(ex) {
        ex.targets.filter { it.role.name == "PRIMARY" }
            .mapNotNull { t -> Catalogo.muscleById[t.muscleId]?.name }
            .joinToString(" · ")
    }
    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(ex.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Patrón: ${ex.pattern}", style = MaterialTheme.typography.bodySmall)
            if (primaryTargets.isNotBlank()) {
                Text("Primario: $primaryTargets", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
