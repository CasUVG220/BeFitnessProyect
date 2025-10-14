package com.befitnessapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    goDashboard: () -> Unit,
    goLibrary: () -> Unit,
    goMuscleMap: () -> Unit,
    goLog: () -> Unit,
    goAddLog: () -> Unit,
    goRecommendations: () -> Unit,
    goRoutines: () -> Unit,
    goCalendar: () -> Unit,
    goProfile: () -> Unit,
    goSettings: () -> Unit
) {
    // solo visual
    val fmt = remember { DateTimeFormatter.ofPattern("EEE d MMM") }
    val today = remember { LocalDate.now() }
    val weekly = remember { WeeklyStats(volume = 12430f, reps = 468, sets = 72, prs = 3) }
    val lastWorkouts = remember {
        listOf(
            UiWorkout("PPL – Push", today.minusDays(1), volume = 3520f, sets = 18, reps = 128),
            UiWorkout("Lower – Squat focus", today.minusDays(3), 4110f, 22, 142),
            UiWorkout("Pull – Back & Bi", today.minusDays(5), 3360f, 19, 120)
        )
    }


    Surface(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(6.dp)) }

            // Cabecera
            item {
                Column(Modifier.fillMaxWidth()) {
                    Text("Tu semana de entrenamiento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Hoy • ${fmt.format(today)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // KPIs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Volumen (sem)", pretty(weekly.volume), container = { primaryContainer }, modifier = Modifier.weight(1f))
                        StatCard("PRs (sem)", "${weekly.prs}", container = { tertiaryContainer }, modifier = Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Reps (sem)", "${weekly.reps}", container = { secondaryContainer }, modifier = Modifier.weight(1f))
                        StatCard("Sets (sem)", "${weekly.sets}", container = { surfaceVariant }, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Acciones rápidas
            item {
                Text("Acciones rápidas", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionTile("Añadir entrenamiento", "Crear un log rápido", onClick = goAddLog, modifier = Modifier.weight(1f))
                        ActionTile("Calendario", "Ver días entrenados", onClick = goCalendar, modifier = Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionTile("Biblioteca", "Buscar ejercicios", onClick = goLibrary, modifier = Modifier.weight(1f))
                        ActionTile("Rutinas", "Tus rutinas guardadas", onClick = goRoutines, modifier = Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionTile("Mapa muscular", "Vista por grupos", onClick = goMuscleMap, modifier = Modifier.weight(1f))
                        ActionTile("Recomendaciones", "Ideas para hoy", onClick = goRecommendations, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Último entrenamiento
            item {
                ElevatedCard {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Último entrenamiento", style = MaterialTheme.typography.titleMedium)
                        val last = lastWorkouts.firstOrNull()
                        if (last == null) {
                            Text("Aún no registras entrenamientos.")
                        } else {
                            Text(last.title, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${fmt.format(last.date)} • Volumen: ${pretty(last.volume)} · Sets: ${last.sets} · Reps: ${last.reps}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            OutlinedButton(onClick = goLog, modifier = Modifier.fillMaxWidth()) {
                                Text("Ver historial")
                            }
                        }
                    }
                }
            }

            // Mini mapa muscular (preview)
            item {
                Text("Mapa muscular (preview)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                MuscleGridPreview(
                    groups = listOf(
                        "Pecho", "Espalda", "Hombros", "Bíceps", "Tríceps", "Antebrazo",
                        "Cuádriceps", "Isquios", "Glúteo", "Pantorrilla", "Core", "Trapecio"
                    ),
                    onClick = { goMuscleMap() }
                )
            }

            // Recientes
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recientes", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = goCalendar) { Text("Ver calendario") }
                }
            }
            items(lastWorkouts) { w ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { goCalendar() }
                ) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(w.title, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${fmt.format(w.date)} • Volumen: ${pretty(w.volume)} · Sets: ${w.sets} · Reps: ${w.reps}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}



private data class WeeklyStats(val volume: Float, val reps: Int, val sets: Int, val prs: Int)
private data class UiWorkout(val title: String, val date: LocalDate, val volume: Float, val sets: Int, val reps: Int)

@Composable
private fun StatCard(
    title: String,
    value: String,
    container: @Composable ColorScheme.() -> androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = container(MaterialTheme.colorScheme),
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .heightIn(min = 100.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MuscleGridPreview(groups: List<String>, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large)
            .padding(12.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = groups.chunked(3)
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { name ->
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        tonalElevation = 1.dp,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(Modifier.height(36.dp), contentAlignment = Alignment.Center) {
                            Text(name, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Toca para ver el mapa completo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun pretty(v: Float): String {
    val s = String.format("%.1f", v)
    return if (s.endsWith(".0")) s.dropLast(2) else s
}
