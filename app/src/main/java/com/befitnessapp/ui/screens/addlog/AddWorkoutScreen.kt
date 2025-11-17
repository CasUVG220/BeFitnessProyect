package com.befitnessapp.ui.screens.addlog

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.catalog.Exercise
import com.befitnessapp.domain.catalog.MuscleGroup
import com.befitnessapp.prefs.AppSettings
import com.befitnessapp.prefs.SettingsState
import com.befitnessapp.prefs.WeightUnit
import com.befitnessapp.routines.RoutinesServiceLocator
import com.befitnessapp.ui.localization.LocalStrings
import com.befitnessapp.ui.screens.routines.RoutinePickerDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AddWorkoutScreen(
    onBack: () -> Unit,
    initialDate: LocalDate? = null
) {
    val vm: AddWorkoutViewModel =
        viewModel(factory = AddWorkoutViewModel.factory(Graph.workoutRepository))

    val strings = LocalStrings.current.addWorkout

    LaunchedEffect(initialDate) {
        initialDate?.let { vm.setDate(it) }
    }

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
                d.year,
                d.monthValue - 1,
                d.dayOfMonth
            ).show()
            showDatePicker = false
        }
    }

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

    val entries by vm.entries.collectAsState()

    val settingsFlow = remember { AppSettings.observe(ctx) }
    val settings by settingsFlow.collectAsState(initial = SettingsState())
    val weightUnit: WeightUnit = settings.weightUnit

    val routinesContext = LocalContext.current
    val routinesRepo = remember(routinesContext) { RoutinesServiceLocator.repository(routinesContext) }
    val routinesState = remember(routinesRepo) { routinesRepo.observeRoutines() }
        .collectAsState(initial = emptyList())
    val routines = routinesState.value
    var showRoutinePicker by remember { mutableStateOf(false) }

    var loadedRoutineName by remember { mutableStateOf<String?>(null) }

    var sheetExercise by remember { mutableStateOf<Exercise?>(null) }
    var sheetSets by remember { mutableStateOf(listOf("10" to "20")) }
    var showSelection by remember { mutableStateOf(false) }
    var summaryData by remember { mutableStateOf<WorkoutSummary?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.title, style = MaterialTheme.typography.headlineSmall)

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${strings.datePrefix} ${dateFmt.format(date)}")
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(strings.searchLabel) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { selectedGroup = null },
                    label = { Text(strings.filterAll) }
                )
                Catalogo.allGroups.forEach { grp ->
                    val selected = selectedGroup?.id == grp.id
                    AssistChip(
                        onClick = { selectedGroup = if (selected) null else grp },
                        label = { Text(grp.name) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    )
                }
                OutlinedButton(onClick = { showRoutinePicker = true }) {
                    Text(strings.loadRoutineButton)
                }
                if (entries.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { showSelection = true }) {
                        Text(strings.viewSelectionButton(entries.count { it.sets.isNotEmpty() }))
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
                        Text(
                            strings.noResults,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val snapshot = entries.mapNotNull { e ->
                        val ex = Catalogo.allExercises.firstOrNull { it.id == e.exerciseId }
                            ?: return@mapNotNull null
                        val sets = e.sets.map { (reps, weightKg) ->
                            SetSnapshot(reps = reps, weight = weightKg)
                        }
                        ExerciseSnapshot(
                            exerciseId = e.exerciseId,
                            exerciseName = ex.name,
                            sets = sets
                        )
                    }

                    if (snapshot.isEmpty()) return@Button

                    vm.save(
                        notes = loadedRoutineName,
                        onDone = { prs ->
                            val computed = computeSummary(snapshot, prs)
                            summaryData = computed
                        },
                        onError = {
                            // futuro: snackbar/toast
                        }
                    )
                },
                enabled = entries.any { it.sets.isNotEmpty() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.logButton)
            }
        }

        sheetExercise?.let { ex ->
            SimpleBottomSheet(
                onDismissRequest = { sheetExercise = null }
            ) {
                Text(ex.name, style = MaterialTheme.typography.titleLarge)
                MuscleInfoCard(ex)
                Text(
                    "Patrón: ${ex.pattern}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(strings.sheetSetsTitle, style = MaterialTheme.typography.titleMedium)
                sheetSets.forEachIndexed { idx, (repsStr, weightStr) ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = repsStr,
                            onValueChange = { new ->
                                val clean = new.filter { it.isDigit() }.take(3)
                                sheetSets = sheetSets.toMutableList().also { it[idx] = clean to weightStr }
                            },
                            label = { Text(strings.sheetRepsLabel) },
                            modifier = Modifier.weight(1f)
                        )
                        val unitLabel = if (weightUnit == WeightUnit.KG) "kg" else "lb"
                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { new ->
                                val clean = buildString {
                                    var dot = false
                                    new.forEach { c ->
                                        when {
                                            c.isDigit() -> append(c)
                                            c == '.' && !dot -> {
                                                append(c)
                                                dot = true
                                            }
                                        }
                                    }
                                }.take(6)
                                sheetSets = sheetSets.toMutableList().also { it[idx] = repsStr to clean }
                            },
                            label = {
                                Text(strings.sheetWeightLabel(unitLabel))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                if (sheetSets.size > 1) {
                                    sheetSets = sheetSets.toMutableList().also { it.removeAt(idx) }
                                }
                            },
                            enabled = sheetSets.size > 1
                        ) {
                            Text(strings.sheetDeleteSetButton)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { sheetSets = sheetSets + ("10" to "20") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.sheetAddSetButton)
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val parsed = sheetSets.mapNotNull { (r, w) ->
                            val reps = r.toIntOrNull()
                            val weightDisplay = w.toFloatOrNull()
                            val weightKg =
                                weightDisplay?.let { fromDisplayWeight(it, weightUnit) } ?: 0f
                            if (reps != null && reps > 0) reps to weightKg else null
                        }
                        if (parsed.isNotEmpty()) {
                            val exId = ex.id
                            if (entries.none { it.exerciseId == exId }) {
                                vm.addExercise(exId)
                                parsed.forEach { (reps, weightKg) ->
                                    vm.addSet(exId, reps = reps, weight = weightKg)
                                }
                            } else {
                                vm.replaceSets(exerciseId = exId, newSets = parsed)
                            }
                            sheetExercise = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.sheetSaveChangesButton)
                }

                Spacer(Modifier.height(12.dp))
            }
        }

        if (showSelection) {
            SimpleBottomSheet(
                onDismissRequest = { showSelection = false }
            ) {
                Text(
                    strings.selectionTitle(entries.count { it.sets.isNotEmpty() }),
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider()

                if (entries.isEmpty()) {
                    Text(
                        strings.selectionEmpty,
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                                        Text(
                                            ex.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        e.sets.forEachIndexed { idx, (reps, wKg) ->
                                            Text(
                                                text = "• Set ${idx + 1}: $reps reps · ${
                                                    formatWeight(wKg, weightUnit)
                                                }",
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
                                                    sheetSets = e.sets.map { (r, wKg) ->
                                                        val display =
                                                            toDisplayWeight(wKg, weightUnit)
                                                        r.toString() to formatFloat(display)
                                                    }
                                                    showSelection = false
                                                }
                                            ) { Text(strings.selectionEditSetsButton) }

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
                                                    contentDescription = strings.selectionRemoveExerciseContentDescription
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
                ) {
                    Text(strings.summaryCloseButton)
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (showRoutinePicker) {
            RoutinePickerDialog(
                routines = routines,
                onPick = { detail ->
                    showRoutinePicker = false

                    loadedRoutineName = detail.routine.name.ifBlank {
                        strings.routineDefaultName
                    }

                    detail.exercises.forEach { ex ->
                        val exId = ex.exerciseId
                        if (entries.none { it.exerciseId == exId }) {
                            vm.addExercise(exId)
                        }
                        ex.sets.forEach { s ->
                            val reps = if (s.reps > 0) s.reps else 1
                            val weightKg = if (s.weight.isFinite()) s.weight else 0f
                            vm.addSet(exerciseId = exId, reps = reps, weight = weightKg)
                        }
                    }
                    showSelection = true
                },
                onDismiss = { showRoutinePicker = false }
            )
        }

        summaryData?.let { summary ->
            SimpleBottomSheet(
                onDismissRequest = {
                    summaryData = null
                    loadedRoutineName = null
                    onBack()
                }
            ) {
                WorkoutSummaryContent(
                    summary = summary,
                    weightUnit = weightUnit,
                    onClose = {
                        summaryData = null
                        loadedRoutineName = null
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
    val topExercisesByVolume: List<ExerciseVolume>,
    val prs: List<ExercisePR>
)

private data class ExerciseVolume(
    val exerciseName: String,
    val volume: Float,
    val reps: Int,
    val sets: Int,
    val topWeight: Float
)

private data class ExercisePR(
    val exerciseName: String,
    val newMax: Float
)

private fun computeSummary(
    snap: List<ExerciseSnapshot>,
    prs: Map<Int, Float>
): WorkoutSummary {
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

    val prList = prs.mapNotNull { (exerciseId, newMax) ->
        val nameFromSnap = snap.firstOrNull { it.exerciseId == exerciseId }?.exerciseName
        val name = nameFromSnap
            ?: Catalogo.allExercises.firstOrNull { it.id == exerciseId }?.name
        name?.let { ExercisePR(exerciseName = it, newMax = newMax) }
    }.sortedByDescending { it.newMax }

    return WorkoutSummary(
        totalVolume = volume,
        totalReps = repsTotal,
        totalSets = setsTotal,
        totalExercises = snap.size,
        topExercisesByVolume = byEx,
        prs = prList
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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
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
    weightUnit: WeightUnit,
    onClose: () -> Unit
) {
    val strings = LocalStrings.current.addWorkout

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(strings.summaryTitle, style = MaterialTheme.typography.titleLarge)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = strings.summaryVolumeTitle,
                    value = formatVolume(summary.totalVolume, weightUnit),
                    container = { primaryContainer }
                )
                StatCard(
                    title = strings.summaryRepsTitle,
                    value = summary.totalReps.toString(),
                    container = { secondaryContainer }
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = strings.summaryExercisesTitle,
                    value = summary.totalExercises.toString(),
                    container = { tertiaryContainer }
                )
                StatCard(
                    title = strings.summarySetsTitle,
                    value = summary.totalSets.toString(),
                    container = { surfaceVariant }
                )
            }
        }

        if (summary.topExercisesByVolume.isNotEmpty()) {
            Text(strings.summaryHighlightsTitle, style = MaterialTheme.typography.titleMedium)
            summary.topExercisesByVolume.forEach { ex ->
                Card {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(ex.exerciseName, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Volumen: ${formatVolume(ex.volume, weightUnit)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Reps: ${ex.reps} · Sets: ${ex.sets} · Peso máx: ${
                                formatWeight(ex.topWeight, weightUnit)
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (summary.prs.isNotEmpty()) {
            Text(strings.summaryPrsTitle, style = MaterialTheme.typography.titleMedium)
            summary.prs.forEach { pr ->
                Text(
                    "• ${pr.exerciseName}: ${formatWeight(pr.newMax, weightUnit)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text(strings.summaryCloseButton)
        }
        Spacer(Modifier.height(8.dp))
    }
}

private const val LB_PER_KG = 2.20462f

private fun toDisplayWeight(kg: Float, unit: WeightUnit): Float =
    if (unit == WeightUnit.KG) kg else kg * LB_PER_KG

private fun fromDisplayWeight(value: Float, unit: WeightUnit): Float =
    if (unit == WeightUnit.KG) value else value / LB_PER_KG

private fun formatWeight(kg: Float, unit: WeightUnit): String {
    val display = toDisplayWeight(kg, unit)
    return formatFloat(display)
}

private fun formatVolume(volumeKgReps: Float, unit: WeightUnit): String {
    val display = if (unit == WeightUnit.KG) volumeKgReps else volumeKgReps * LB_PER_KG
    return formatFloat(display)
}

private fun formatFloat(value: Float): String {
    val s = String.format(Locale.US, "%.1f", value)
    return if (s.endsWith(".0")) s.dropLast(2) else s
}

@Composable
private fun SimpleBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismissRequest() }
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}
