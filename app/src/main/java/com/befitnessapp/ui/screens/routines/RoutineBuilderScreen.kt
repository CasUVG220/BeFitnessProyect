package com.befitnessapp.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.catalog.Exercise
import com.befitnessapp.domain.catalog.MuscleGroup
import com.befitnessapp.routines.RoutinesServiceLocator
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun RoutineBuilderScreen(
    vm: RoutineBuilderViewModel = viewModel(),
    onBack: () -> Unit
) {
    val ui by vm.uiState.collectAsState()
    val ctx = LocalContext.current
    val repo = remember(ctx) { RoutinesServiceLocator.repository(ctx) }
    val scope = rememberCoroutineScope()

    var showPicker by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }
    var editSets by remember { mutableStateOf(listOf("10" to "20")) } // (reps, peso) — peso no se persiste
    var showEmptyWarn by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (ui.routineId == null) "Nueva rutina" else "Editar rutina",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onBack) { Text("Volver") }
            }

            OutlinedTextField(
                value = ui.name,
                onValueChange = vm::setName,
                label = { Text("Nombre de la rutina") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { showPicker = true }) { Text("Agregar ejercicio") }
                val canSave = ui.name.isNotBlank() && ui.exercises.any { it.sets.isNotEmpty() }
                Button(
                    onClick = {
                        if (!canSave) { showEmptyWarn = true; return@Button }
                        val payload: List<Pair<Int, Int>> =
                            ui.exercises.map { it.exerciseId to it.sets.size }

                        scope.launch {
                            repo.saveRoutine(
                                name = ui.name.trim(),
                                items = payload
                            )
                            onBack()
                        }
                    },
                    enabled = canSave
                ) { Text("Guardar") }
            }

            Text("Contenido", style = MaterialTheme.typography.titleMedium)
            if (ui.exercises.isEmpty()) {
                Text("Aún no has agregado ejercicios.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ui.exercises) { item ->
                        val ex = Catalogo.allExercises.firstOrNull { it.id == item.exerciseId }
                        if (ex != null) {
                            Card {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(ex.name, style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.weight(1f))
                                        IconButton(onClick = { vm.removeExercise(item.exerciseId) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                                        }
                                    }

                                    if (item.sets.isEmpty()) {
                                        Text("— sin sets —", style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        Text(
                                            "Sets: ${item.sets.size}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                editingExercise = ex
                                                editSets =
                                                    if (item.sets.isEmpty()) listOf("10" to "20")
                                                    else item.sets.map { (r, w) ->
                                                        r.toString() to formatFloat(w)
                                                    }
                                                showEdit = true
                                            }
                                        ) { Text("Editar sets") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ---------- Overlay: Picker ----------
        if (showPicker) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { },
                color = Color.Transparent
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Surface(
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        ExercisePickerContent(
                            onPick = { ex ->
                                showPicker = false
                                editingExercise = ex
                                editSets = listOf("10" to "20")
                                showEdit = true
                            },
                            onClose = { showPicker = false }
                        )
                    }
                }
            }
        }

        // ---------- Overlay: Editor de sets ----------
        if (showEdit && editingExercise != null) {
            val ex = editingExercise!!
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { },
                color = Color.Transparent
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Surface(
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ex.name, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { showEdit = false }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                                }
                            }
                            Text("Patrón: ${ex.pattern}", style = MaterialTheme.typography.bodyMedium)

                            editSets.forEachIndexed { idx, (repsStr, weightStr) ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = repsStr,
                                        onValueChange = { new ->
                                            val clean = new.filter { it.isDigit() }.take(3)
                                            editSets = editSets.toMutableList().also { it[idx] = clean to weightStr }
                                        },
                                        label = { Text("Reps") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = weightStr,
                                        onValueChange = { new ->
                                            val clean = buildString {
                                                var dot = false
                                                new.forEach { c ->
                                                    when {
                                                        c.isDigit() -> append(c)
                                                        c == '.' && !dot -> { append(c); dot = true }
                                                    }
                                                }
                                            }.take(6)
                                            editSets = editSets.toMutableList().also { it[idx] = repsStr to clean }
                                        },
                                        label = { Text("Peso (no se guarda)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            if (editSets.size > 1) {
                                                editSets = editSets.toMutableList().also { it.removeAt(idx) }
                                            }
                                        },
                                        enabled = editSets.size > 1
                                    ) { Text("Eliminar") }
                                }
                            }

                            OutlinedButton(
                                onClick = { editSets = editSets + ("10" to "20") },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Añadir set") }

                            Button(
                                onClick = {
                                    val parsed = editSets.mapNotNull { (r, w) ->
                                        val reps = r.toIntOrNull()
                                        val weight = w.toFloatOrNull()
                                        if (reps != null && reps > 0) reps to (weight ?: 0f) else null
                                    }
                                    if (parsed.isNotEmpty()) {
                                        if (ui.exercises.none { it.exerciseId == ex.id }) {
                                            vm.addExercise(ex.id)
                                        }
                                        vm.replaceSets(ex.id, parsed)
                                        showEdit = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Guardar cambios") }
                        }
                    }
                }
            }
        }
    }

    if (showEmptyWarn) {
        AlertDialog(
            onDismissRequest = { showEmptyWarn = false },
            confirmButton = { Button(onClick = { showEmptyWarn = false }) { Text("Ok") } },
            title = { Text("Rutina incompleta") },
            text = { Text("Añade al menos un ejercicio con sets y un nombre para poder guardar.") }
        )
    }
}

/* ---------------- Picker de ejercicios (simple) ---------------- */

@Composable
private fun ExercisePickerContent(
    onPick: (Exercise) -> Unit,
    onClose: () -> Unit
) {
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

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Elegir ejercicio", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(text = "Todos", selected = selectedGroup == null) { selectedGroup = null }
            Catalogo.allGroups.forEach { grp ->
                FilterChip(
                    text = grp.name,
                    selected = selectedGroup?.id == grp.id
                ) {
                    selectedGroup = if (selectedGroup?.id == grp.id) null else grp
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { ex ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(ex) }
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(ex.name, style = MaterialTheme.typography.titleMedium)
                        Text("Patrón: ${ex.pattern}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (results.isEmpty()) {
                item { Text("No se encontraron ejercicios.", style = MaterialTheme.typography.bodyMedium) }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = onClose) { Text("Cerrar") }
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        tonalElevation = if (selected) 2.dp else 0.dp,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatFloat(value: Float): String {
    val s = String.format(Locale.US, "%.1f", value)
    return if (s.endsWith(".0")) s.dropLast(2) else s
}
