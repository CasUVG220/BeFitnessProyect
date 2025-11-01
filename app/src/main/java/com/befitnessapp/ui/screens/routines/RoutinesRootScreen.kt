package com.befitnessapp.ui.screens.routines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.befitnessapp.routines.RoutinesServiceLocator
import com.befitnessapp.domain.routines.RoutineDetail // tipo del repositorio

/** Entrada pública usada por el AppNavHost. */
@Composable
fun RoutinesScreen(onBack: () -> Unit) {
    RoutinesRootScreen(onBack = onBack)
}

/** Pantalla raíz: Inicio → Lista → Builder */
@Composable
private fun RoutinesRootScreen(onBack: () -> Unit) {
    var mode by remember { mutableStateOf(Mode.Home) }

    when (mode) {
        Mode.Home -> RoutinesHome(
            onBack = onBack,
            goList = { mode = Mode.List },
            goBuilder = { mode = Mode.Builder }
        )
        Mode.List -> RoutinesList(
            onBack = { mode = Mode.Home },
            goBuilder = { mode = Mode.Builder }
        )
        Mode.Builder -> RoutineBuilderScreen(
            onBack = { mode = Mode.Home }
        )
    }
}

private enum class Mode { Home, List, Builder }

/* --------------------------- UI: Home --------------------------- */

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header simple (100% estable)
            Surface(tonalElevation = 1.dp) {
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
            }

            Text("¿Qué deseas hacer?", style = MaterialTheme.typography.titleMedium)

            Button(onClick = goList, modifier = Modifier.fillMaxWidth()) {
                Text("Mis rutinas")
            }
            OutlinedButton(onClick = goBuilder, modifier = Modifier.fillMaxWidth()) {
                Text("Crear / Editar")
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Crea una rutina con ejercicios y sets base. Luego, desde “Añadir workout” puedes cargarla y ajustar sin modificar la rutina guardada.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/* --------------------------- UI: List --------------------------- */

@Composable
private fun RoutinesList(
    onBack: () -> Unit,
    goBuilder: () -> Unit
) {
    val ctx = LocalContext.current
    val repo = remember(ctx) { RoutinesServiceLocator.repository(ctx) }

    // Tipado explícito para evitar inferencias raras
    val routines: List<RoutineDetail> by remember(repo) {
        repo.observeRoutines()
    }.collectAsState(initial = emptyList())

    Scaffold { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header simple
            Surface(tonalElevation = 1.dp) {
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
                itemsIndexed(routines) { index, r ->
                    val totalExercises = r.exercises.size
                    val totalSets = r.exercises.sumOf { it.sets.size }

                    Card {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Título genérico para no depender del nombre del campo (name/title/nombre)
                            Text("Rutina ${index + 1}", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Ejercicios: $totalExercises · Sets totales: $totalSets",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Footer simple
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onBack) { Text("Volver") }
                Spacer(Modifier.weight(1f))
                Button(onClick = goBuilder) { Text("Crear nueva") }
            }
        }
    }
}
