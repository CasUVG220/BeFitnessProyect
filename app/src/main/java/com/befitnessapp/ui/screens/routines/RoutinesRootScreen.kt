package com.befitnessapp.ui.screens.routines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.befitnessapp.domain.routines.RoutineDetail
import com.befitnessapp.routines.RoutinesServiceLocator
import com.befitnessapp.ui.screens.routines.nav.RoutinesRoute
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun RoutinesScreen(onBack: () -> Unit) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = RoutinesRoute.Home) {

        // Home del módulo de Rutinas
        composable<RoutinesRoute.Home> {
            RoutinesHome(
                onBack = onBack,
                goList = { nav.navigate(RoutinesRoute.List) },
                goBuilder = { nav.navigate(RoutinesRoute.Builder()) }
            )
        }

        // Lista de rutinas
        composable<RoutinesRoute.List> {
            RoutinesList(
                onBack = { nav.popBackStack() },
                goBuilder = { nav.navigate(RoutinesRoute.Builder()) },
                onOpen = { detail -> nav.navigate(RoutinesRoute.Builder(detail.routine.id)) }
            )
        }

        // Builder: crear/editar
        composable<RoutinesRoute.Builder> { backStackEntry ->
            val args = backStackEntry.toRoute<RoutinesRoute.Builder>()
            val ctx = LocalContext.current
            val repo = remember(ctx) { RoutinesServiceLocator.repository(ctx) }
            var initial by remember { mutableStateOf<RoutineDetail?>(null) }

            LaunchedEffect(args.routineId) {
                initial = args.routineId?.let { repo.getRoutine(it) }
            }

            RoutineBuilderScreen(
                onBack = { nav.popBackStack() },
                initial = initial
            )
        }
    }
}

@Composable
private fun RoutinesHome(
    onBack: () -> Unit,
    goList: () -> Unit,
    goBuilder: () -> Unit
) {
    Scaffold { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rutinas", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onBack) { Text("Volver") }
            }

            Text("¿Qué deseas hacer?", style = MaterialTheme.typography.titleMedium)

            Button(onClick = goList, modifier = Modifier.fillMaxWidth()) { Text("Mis rutinas") }
            OutlinedButton(onClick = goBuilder, modifier = Modifier.fillMaxWidth()) { Text("Crear nueva") }
        }
    }
}

@Composable
private fun RoutinesList(
    onBack: () -> Unit,
    goBuilder: () -> Unit,
    onOpen: (RoutineDetail) -> Unit
) {
    val ctx = LocalContext.current
    val repo = remember(ctx) { RoutinesServiceLocator.repository(ctx) }
    val scope = rememberCoroutineScope()

    var routines by remember { mutableStateOf<List<RoutineDetail>>(emptyList()) }
    LaunchedEffect(repo) {
        repo.observeRoutines().collectLatest { routines = it }
    }

    var confirmDelete by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteName by remember { mutableStateOf("") }

    Scaffold { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mis rutinas", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onBack) { Text("Volver") }
            }

            if (routines.isEmpty()) {
                Text("Aún no tienes rutinas. Crea una desde “Crear nueva”.")
                OutlinedButton(onClick = goBuilder) { Text("Crear nueva") }
                return@Column
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(routines) { _, r ->
                    val totalExercises = r.exercises.size
                    val totalSets = r.exercises.sumOf { it.sets.size }

                    Card {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(r.routine.name.ifBlank { "Rutina" }, style = MaterialTheme.typography.titleMedium)
                            Text("Ejercicios: $totalExercises · Sets totales: $totalSets", style = MaterialTheme.typography.bodySmall)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(onClick = { onOpen(r) }) { Text("Editar / Abrir") }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(onClick = {
                                    pendingDeleteId = r.routine.id
                                    pendingDeleteName = r.routine.name.ifBlank { "Rutina" }
                                    confirmDelete = true
                                }) { Text("Borrar", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Volver") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = goBuilder, modifier = Modifier.weight(1f)) { Text("Crear nueva") }
            }
        }
    }

    if (confirmDelete && pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                Button(onClick = {
                    val id = pendingDeleteId!!
                    confirmDelete = false
                    scope.launch { repo.deleteRoutine(id) }
                }) { Text("Borrar") }
            },
            dismissButton = { OutlinedButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
            title = { Text("Borrar rutina") },
            text = { Text("¿Seguro que deseas borrar “$pendingDeleteName”? Esta acción no se puede deshacer.") }
        )
    }
}
