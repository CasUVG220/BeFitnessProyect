package com.befitnessapp.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.catalog.Exercise
import com.befitnessapp.domain.catalog.MuscleGroup
import com.befitnessapp.domain.routines.RoutineDetail
import com.befitnessapp.routines.RoutinesServiceLocator
import com.befitnessapp.ui.localization.LocalStrings
import kotlinx.coroutines.launch

@Composable
fun RoutineBuilderScreen(
    vm: RoutineBuilderViewModel = viewModel(),
    onBack: () -> Unit,
    initial: RoutineDetail? = null
) {
    val ui by vm.uiState.collectAsState()
    val ctx = LocalContext.current
    val repo = remember(ctx) { RoutinesServiceLocator.repository(ctx) }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    LaunchedEffect(initial) {
        if (initial != null) {
            vm.loadFrom(initial)
        }
    }

    var showPicker by remember { mutableStateOf(false) }
    var showEmptyWarn by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (ui.routineId == null) {
                    strings.routines.builderNewTitle
                } else {
                    strings.routines.builderEditTitle
                },
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.weight(1f))
            if (ui.routineId != null) {
                OutlinedButton(onClick = { showDeleteConfirm = true }) {
                    Text(strings.routines.builderDeleteButton)
                }
                Spacer(Modifier.width(8.dp))
            }
            OutlinedButton(onClick = onBack) {
                Text(strings.routines.builderBackButton)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = ui.name,
                onValueChange = vm::setName,
                label = { Text(strings.routines.builderNameLabel) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(12.dp))
            Button(onClick = { showPicker = true }) {
                Text(strings.routines.builderAddExerciseButton)
            }
        }

        Text(
            strings.routines.builderContentTitle,
            style = MaterialTheme.typography.titleMedium
        )

        if (ui.exercises.isEmpty()) {
            Text(strings.routines.builderEmptyExercises)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ui.exercises) { item ->
                    val ex = Catalogo.allExercises.firstOrNull { it.id == item.exerciseId }
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
                                Text(
                                    ex?.name ?: strings.routines.builderExerciseFallback,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.weight(1f))
                                OutlinedButton(onClick = { vm.removeExercise(item.exerciseId) }) {
                                    Text(strings.routines.builderRemoveExerciseButton)
                                }
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${strings.routines.builderSetsLabel} ${item.sets.size}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                OutlinedButton(
                                    onClick = {
                                        vm.changeSetsCount(
                                            item.exerciseId,
                                            (item.sets.size - 1).coerceAtLeast(1)
                                        )
                                    }
                                ) {
                                    Text("-")
                                }
                                OutlinedButton(
                                    onClick = {
                                        vm.changeSetsCount(
                                            item.exerciseId,
                                            item.sets.size + 1
                                        )
                                    }
                                ) {
                                    Text("+")
                                }
                            }
                        }
                    }
                }
            }
        }

        val canSave = ui.name.isNotBlank() && ui.exercises.any { it.sets.isNotEmpty() }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onBack
            ) {
                Text(strings.routines.builderCancelButton)
            }
            Button(
                modifier = Modifier.weight(1f),
                enabled = canSave,
                onClick = {
                    if (!canSave) {
                        showEmptyWarn = true
                        return@Button
                    }
                    scope.launch {
                        val payload: List<Pair<Int, Int>> =
                            ui.exercises.map { it.exerciseId to it.sets.size }
                        if (ui.routineId == null) {
                            repo.saveRoutine(name = ui.name.trim(), items = payload)
                        } else {
                            repo.updateRoutine(
                                id = ui.routineId!!,
                                name = ui.name.trim(),
                                items = payload
                            )
                        }
                        onBack()
                    }
                }
            ) {
                Text(
                    if (ui.routineId == null) {
                        strings.routines.builderSaveNewButton
                    } else {
                        strings.routines.builderSaveEditButton
                    }
                )
            }
        }
    }

    if (showPicker) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable { },
            color = Color.Transparent
        ) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
                            if (ui.exercises.none { it.exerciseId == ex.id }) {
                                vm.addExercise(ex.id)
                            }
                        },
                        onClose = { showPicker = false }
                    )
                }
            }
        }
    }

    if (showEmptyWarn) {
        AlertDialog(
            onDismissRequest = { showEmptyWarn = false },
            confirmButton = {
                Button(onClick = { showEmptyWarn = false }) {
                    Text(LocalStrings.current.routines.incompleteOkButton)
                }
            },
            title = { Text(LocalStrings.current.routines.incompleteTitle) },
            text = { Text(LocalStrings.current.routines.incompleteText) }
        )
    }

    if (showDeleteConfirm && ui.routineId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            repo.deleteRoutine(ui.routineId!!)
                            onBack()
                        }
                    }
                ) {
                    Text(LocalStrings.current.routines.deleteDialogConfirm)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text(LocalStrings.current.routines.deleteDialogCancel)
                }
            },
            title = { Text(LocalStrings.current.routines.deleteDialogTitle) },
            text = { Text(LocalStrings.current.routines.deleteDialogText) }
        )
    }
}

@Composable
private fun ExercisePickerContent(
    onPick: (Exercise) -> Unit,
    onClose: () -> Unit
) {
    val strings = LocalStrings.current

    var query by remember { mutableStateOf("") }
    var selectedGroup: MuscleGroup? by remember { mutableStateOf(null) }
    val results = remember(query, selectedGroup) {
        Catalogo.searchExercises(
            query = query,
            groupId = selectedGroup?.id
        )
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(strings.routines.pickerTitle, style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(strings.routines.pickerSearchLabel) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                text = strings.routines.pickerFilterAll,
                selected = selectedGroup == null
            ) { selectedGroup = null }

            Catalogo.allGroups.forEach { grp ->
                FilterChip(
                    text = grp.name,
                    selected = selectedGroup?.id == grp.id
                ) {
                    selectedGroup =
                        if (selectedGroup?.id == grp.id) null else grp
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
                        Text(
                            "${strings.routines.pickerPatternPrefix} ${ex.pattern}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            if (results.isEmpty()) {
                item {
                    Text(
                        strings.routines.pickerNoResults,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onClose) {
                Text(strings.routines.pickerCloseButton)
            }
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
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
