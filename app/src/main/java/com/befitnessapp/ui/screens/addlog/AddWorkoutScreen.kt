package com.befitnessapp.ui.screens.addlog

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.catalog.Exercise
import com.befitnessapp.domain.catalog.MuscleGroup
import com.befitnessapp.routines.RoutinesServiceLocator
import com.befitnessapp.ui.screens.routines.RoutinePickerDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddWorkoutScreen(
    onBack: () -> Unit,
    initialDate: LocalDate? = null
) {
    val vm: AddWorkoutViewModel =
        viewModel(factory = AddWorkoutViewModel.factory(Graph.workoutRepository))

    // fecha inicial (desde calendario)
    LaunchedEffect(initialDate) {
        initialDate?.let { vm.setDate(it) }
    }

    // ===== fecha objetivo =====
    val date by vm.date.collectAsState()
    val ctx = LocalContext.current
    val dateFmt = remember { DateTimeFormatter.ISO_DATE }
    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        LaunchedEffect(Unit) {
            val d = date
            DatePickerDialog(
                ctx,
                { _, y, m, day -> vm.setDate(LocalDate.of(y, m + 1, day)) },
                d.year, d.monthValue - 1, d.dayOfMonth
            ).show()
            showDatePicker = false
        }
    }

    // búsqueda / filtro
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

    // ============= Sheets ============
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sheetExercise by remember { mutableStateOf<Exercise?>(null) }
    var sheetSets by remember { mutableStateOf(listOf("10" to "20")) } // (reps, peso) como strings

    val summarySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var summaryData by remember { mutableStateOf<WorkoutSummary?>(null) }

    val selectionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSelection by remember { mutableStateOf(false) }

    val entries by vm.entries.collectAsState()

    // nombre de rutina cargada para el home por si (si se usó "Cargar rutina")
    var loadedRoutineName by remember { mutableStateOf<String?>(null) }

    // ===== Rutinas
    val routinesContext = LocalContext.current
    val routinesRepo = remember(routinesContext) { RoutinesServiceLocator.repository(routinesContext) }
    val routinesState = remember(routinesRepo) { routinesRepo.observeRoutines() }
        .collectAsState(initial = emptyList())
    val routines = routinesState.value
    var showRoutinePicker by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Añadir ejercicio al workout", style = MaterialTheme.typography.headlineSmall)

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Fecha: ${dateFmt.format(date)}")
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar ejercicio") },
                modifier = Modifier.fillMaxWidth()
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = { selectedGroup = null }, label = { Text("Todos") })
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
                OutlinedButton(onClick = { showRoutinePicker = true }) {
                    Text("Cargar rutina")
                }
                if (entries.isNotEmpty()) {
                    OutlinedButton(onClick = { showSelection = true }) {
                        Text("Ver selección (${entries.count { it.sets.isNotEmpty() }})")
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { ex ->
                    ExerciseRow(
                        exercise = ex,
                        onAdd = {
                            sheetExercise = ex
                            sheetSets = listOf("10" to "20")
                        }
                    )
                }
                if (results.isEmpty()) {
                    item {
                        Text("No se encontraron ejercicios.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Button(
                onClick = {
                    val snapshot = entries.mapNotNull { e ->
                        val ex = Catalogo.allExercises.firstOrNull { it.id == e.exerciseId }
                            ?: return@mapNotNull null
                        val sets = e.sets.map { (reps, weight) ->
                            SetSnapshot(reps = reps, weight = weight)
                        }
                        ExerciseSnapshot(
                            exerciseId = e.exerciseId,
                            exerciseName = ex.name,
                            sets = sets
                        )
                    }
                    val computed = computeSummary(snapshot)
                    summaryData = computed

                    vm.save(
                        notes = loadedRoutineName,
                        onDone = { /* abre sheet */ },
                        onError = { /* TODO snack */ }
                    )
                },
                enabled = entries.any { it.sets.isNotEmpty() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Loggear Workout") }
        }

        // SHEET: Editor por ejercicio
        if (sheetExercise != null) {
            ModalBottomSheet(
                onDismissRequest = { sheetExercise = null },
                sheetState = editSheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                val ex = sheetExercise!!
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(ex.name, style = MaterialTheme.typography.titleLarge)
                    MuscleInfoCard(ex)
                    Text("Patrón: ${ex.pattern}", style = MaterialTheme.typography.bodyMedium)

                    Text("Sets", style = MaterialTheme.typography.titleMedium)
                    sheetSets.forEachIndexed { idx, (repsStr, weightStr) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = repsStr,
                                onValueChange = { new ->
                                    val clean = new.filter { it.isDigit() }.take(3)
                                    sheetSets = sheetSets.toMutableList().also { it[idx] = clean to weightStr }
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
                                                c == '.' && !dot -> {
                                                    append(c); dot = true
                                                }
                                            }
                                        }
                                    }.take(6)
                                    sheetSets = sheetSets.toMutableList().also { it[idx] = repsStr to clean }
                                },
                                label = { Text("Peso") },
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    if (sheetSets.size > 1) {
                                        sheetSets = sheetSets.toMutableList().also {
                                            it.removeAt(idx)
                                        }
                                    }
                                },
                                enabled = sheetSets.size > 1
                            ) { Text("Eliminar") }
                        }
                    }

                    OutlinedButton(
                        onClick = { sheetSets = sheetSets + ("10" to "20") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Añadir set") }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val parsed = sheetSets.mapNotNull { (r, w) ->
                                val reps = r.toIntOrNull()
                                val weight = w.toFloatOrNull()
                                if (reps != null && reps > 0) reps to (weight ?: 0f) else null
                            }
                            if (parsed.isNotEmpty()) {
                                val exId = ex.id
                                if (entries.none { it.exerciseId == exId }) {
                                    vm.addExercise(exId)
                                    parsed.forEach { (reps, weight) ->
                                        vm.addSet(exId, reps = reps, weight = weight)
                                    }
                                } else {
                                    vm.replaceSets(exerciseId = exId, newSets = parsed)
                                }
                                sheetExercise = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Guardar cambios") }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // SHEET: Selección actual
        if (showSelection) {
            ModalBottomSheet(
                onDismissRequest = { showSelection = false },
                sheetState = selectionSheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Tu selección (${entries.count { it.sets.isNotEmpty() }})",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()

                    if (entries.isEmpty()) {
                        Text("Aún no has añadido ejercicios.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(entries) { e ->
                                val ex = Catalogo.allExercises.firstOrNull { it.id == e.exerciseId }
                                if (ex != null) {
                                    Card {
                                        Column(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(ex.name, style = MaterialTheme.typography.titleMedium)
                                            e.sets.forEachIndexed { idx, (reps, w) ->
                                                Text(
                                                    "• Set ${idx + 1}: $reps reps · ${formatFloat(w)} kg",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Row(
                                                Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        sheetExercise = ex
                                                        sheetSets = e.sets.map { (r, w) ->
                                                            r.toString() to formatFloat(w)
                                                        }
                                                        showSelection = false
                                                    }
                                                ) { Text("Editar sets") }

                                                IconButton(
                                                    onClick = {
                                                        val exId = e.exerciseId
                                                        val remaining = entries.size - 1
                                                        vm.removeExercise(exId)
                                                        if (remaining <= 0) showSelection = false
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "Quitar ejercicio"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showSelection = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cerrar") }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        //  DIALOG: Picker de Rutina
        if (showRoutinePicker) {
            RoutinePickerDialog(
                routines = routines,
                onPick = { detail ->
                    showRoutinePicker = false

                    // guardamos el nombre REAL de la rutina aplicada
                    loadedRoutineName = detail.routine.name

                    detail.exercises.forEach { ex ->
                        val exId = ex.exerciseId
                        if (entries.none { it.exerciseId == exId }) {
                            vm.addExercise(exId)
                        }
                        ex.sets.forEach { s ->
                            val reps = if (s.reps > 0) s.reps else 1
                            val weight = if (s.weight.isFinite()) s.weight else 0f
                            vm.addSet(exerciseId = exId, reps = reps, weight = weight)
                        }
                    }
                    showSelection = true
                },
                onDismiss = { showRoutinePicker = false }
            )
        }

        // SHEET: Resumen final
        summaryData?.let { summary ->
            ModalBottomSheet(
                onDismissRequest = { /* sin dismiss externo */ },
                sheetState = summarySheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                WorkoutSummaryContent(
                    summary = summary,
                    onClose = {
                        summaryData = null
                        onBack()
                    }
                )
            }
        }
    }
}


private data class SetSnapshot(val reps: Int, val weight: Float)
private data class ExerciseSnapshot(
    val exerciseId: Int,
    val exerciseName: String,
    val sets: List<SetSnapshot>
)
private data class WorkoutSummary(
    val totalVolume: Float,
    val totalReps: Int,
    val totalSets: Int,
    val totalExercises: Int,
    val topExercisesByVolume: List<ExerciseVolume>
)
private data class ExerciseVolume(
    val exerciseName: String,
    val volume: Float,
    val reps: Int,
    val sets: Int,
    val topWeight: Float
)

private fun computeSummary(snap: List<ExerciseSnapshot>): WorkoutSummary {
    var volume = 0f
    var repsTotal = 0
    var setsTotal = 0

    val byEx = snap.map { ex ->
        var v = 0f
        var r = 0
        var s = 0
        var top = 0f
        ex.sets.forEach { set ->
            v += set.reps * set.weight
            r += set.reps
            s += 1
            if (set.weight > top) top = set.weight
        }
        volume += v
        repsTotal += r
        setsTotal += s
        ExerciseVolume(
            exerciseName = ex.exerciseName,
            volume = v,
            reps = r,
            sets = s,
            topWeight = top
        )
    }.sortedByDescending { it.volume }
        .take(3)

    return WorkoutSummary(
        totalVolume = volume,
        totalReps = repsTotal,
        totalSets = setsTotal,
        totalExercises = snap.size,
        topExercisesByVolume = byEx
    )
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    onAdd: () -> Unit
) {
    Card {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            Text("Patrón: ${exercise.pattern}", style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val primarios = exercise.targets
                    .filter { it.role.name == "PRIMARY" }
                    .mapNotNull { t -> Catalogo.muscleById[t.muscleId]?.name }
                    .joinToString(" · ")
                Text(
                    text = if (primarios.isBlank()) "—" else "Primario: $primarios",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onAdd) { Text("Añadir") }
            }
        }
    }
}

@Composable
private fun MuscleInfoCard(exercise: Exercise) {
    val primarios = remember(exercise) {
        exercise.targets.filter { it.role.name == "PRIMARY" }
            .mapNotNull { t -> Catalogo.muscleById[t.muscleId]?.name }
            .joinToString(" · ")
    }
    val secundarios = remember(exercise) {
        exercise.targets.filter { it.role.name == "SECONDARY" }
            .mapNotNull { t -> Catalogo.muscleById[t.muscleId]?.name }
            .joinToString(" · ")
    }

    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(160.dp)) {
                Text(
                    "Vista muscular (placeholder)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
            if (primarios.isNotBlank()) {
                Text("Primario: $primarios", style = MaterialTheme.typography.bodySmall)
            }
            if (secundarios.isNotBlank()) {
                Text("Secundario: $secundarios", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    container: ColorScheme.() -> androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = container(MaterialTheme.colorScheme),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun WorkoutSummaryContent(
    summary: WorkoutSummary,
    onClose: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Resumen del workout", style = MaterialTheme.typography.titleLarge)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Volumen total", formatFloat(summary.totalVolume)) { primaryContainer }
                StatCard("Reps totales", "${summary.totalReps}") { secondaryContainer }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Ejercicios", "${summary.totalExercises}") { tertiaryContainer }
                StatCard("Sets", "${summary.totalSets}") { surfaceVariant }
            }
        }

        if (summary.topExercisesByVolume.isNotEmpty()) {
            Text("Highlights", style = MaterialTheme.typography.titleMedium)
            summary.topExercisesByVolume.forEach { ex ->
                Card {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(ex.exerciseName, style = MaterialTheme.typography.titleSmall)
                        Text("Volumen: ${formatFloat(ex.volume)}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Reps: ${ex.reps} · Sets: ${ex.sets} · Peso máx: ${formatFloat(ex.topWeight)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") }
        Spacer(Modifier.height(8.dp))
    }
}

private fun formatFloat(value: Float): String {
    val s = String.format(Locale.US, "%.1f", value)
    return if (s.endsWith(".0")) s.dropLast(2) else s
}
