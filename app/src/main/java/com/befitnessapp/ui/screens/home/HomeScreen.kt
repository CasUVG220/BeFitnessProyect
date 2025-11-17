package com.befitnessapp.ui.screens.home

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.R
import com.befitnessapp.prefs.AppSettings
import com.befitnessapp.prefs.SettingsState
import com.befitnessapp.prefs.WeightUnit
import com.befitnessapp.ui.localization.LocalStrings
import com.befitnessapp.utils.formatVolumeFromKg
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    goSettings: () -> Unit,
    vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(Graph.workoutRepository))
) {
    val state by vm.uiState.collectAsState()

    val ctx = LocalContext.current
    // 🔄 Leer siempre el flujo actual de settings (sin remember)
    val settingsFlow = AppSettings.observe(context = ctx)
    val settings by settingsFlow.collectAsState(initial = SettingsState())

    val strings = LocalStrings.current.home

    val weightUnit: WeightUnit = settings.weightUnit
    val weeklyGoalKg: Float = settings.weeklyGoal

    val fmt = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())

    val weeklyVolumeText = formatVolumeFromKg(
        valueKg = state.weekly.volume,
        unit = weightUnit
    )

    // 🔄 Progreso siempre recalculado con la meta actual
    val weeklyProgress =
        if (weeklyGoalKg <= 0f) 0f
        else (state.weekly.volume / weeklyGoalKg).coerceIn(0f, 1f)

    Surface(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(6.dp)) }

            // Título + Hoy
            item {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        strings.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${strings.todayPrefix} • ${fmt.format(state.today)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cards de stats semanales
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = strings.weeklyVolumeTitle,
                            value = weeklyVolumeText,
                            container = { primaryContainer },
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = strings.weeklyPrsTitle,
                            value = "${state.weekly.prs}",
                            container = { tertiaryContainer },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = strings.weeklyRepsTitle,
                            value = "${state.weekly.reps}",
                            container = { secondaryContainer },
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = strings.weeklySetsTitle,
                            value = "${state.weekly.sets}",
                            container = { surfaceVariant },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (weeklyGoalKg > 0f) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    strings.weeklyProgressTitle,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                val doneText = formatVolumeFromKg(state.weekly.volume, weightUnit)
                                val goalText = formatVolumeFromKg(weeklyGoalKg, weightUnit)

                                Text(
                                    strings.weeklyProgressValue(doneText, goalText),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LinearProgressIndicator(
                                    progress = { weeklyProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Acciones rápidas
            item {
                Text(strings.quickActionsTitle, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionTile(
                            title = strings.actionAddWorkoutTitle,
                            subtitle = strings.actionAddWorkoutSubtitle,
                            onClick = goAddLog,
                            modifier = Modifier.weight(1f)
                        )
                        ActionTile(
                            title = strings.actionCalendarTitle,
                            subtitle = strings.actionCalendarSubtitle,
                            onClick = goCalendar,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionTile(
                            title = strings.actionLibraryTitle,
                            subtitle = strings.actionLibrarySubtitle,
                            onClick = goLibrary,
                            modifier = Modifier.weight(1f)
                        )
                        ActionTile(
                            title = strings.actionRoutinesTitle,
                            subtitle = strings.actionRoutinesSubtitle,
                            onClick = goRoutines,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionTile(
                            title = strings.actionMuscleMapTitle,
                            subtitle = strings.actionMuscleMapSubtitle,
                            onClick = goMuscleMap,
                            modifier = Modifier.weight(1f)
                        )
                        ActionTile(
                            title = strings.actionProfileTitle,
                            subtitle = strings.actionProfileSubtitle,
                            onClick = goProfile,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionTile(
                            title = strings.actionRecommendationsTitle,
                            subtitle = strings.actionRecommendationsSubtitle,
                            onClick = goRecommendations,
                            modifier = Modifier.weight(1f)
                        )
                        ActionTile(
                            title = strings.actionSettingsTitle,
                            subtitle = strings.actionSettingsSubtitle,
                            onClick = goSettings,
                            modifier = Modifier.weight(1f)
                        )
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
                        Text(strings.lastWorkoutTitle, style = MaterialTheme.typography.titleMedium)
                        val last = state.lastWorkouts.firstOrNull()
                        if (last == null) {
                            Text(strings.lastWorkoutEmpty)
                        } else {
                            Text(last.title, fontWeight = FontWeight.SemiBold)

                            val dateStr = fmt.format(last.date)
                            val volStr = formatVolumeFromKg(last.volume, weightUnit)
                            Text(
                                strings.lastWorkoutInfo(
                                    dateStr,
                                    volStr,
                                    last.sets,
                                    last.reps
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = goLog,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(strings.lastWorkoutHistoryButton)
                            }
                        }
                    }
                }
            }

            // Preview de mapa muscular (imagen grande)
            item {
                MusclePreviewCard(
                    onOpenMuscleMap = goMuscleMap
                )
            }

            // Recientes
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.recentsTitle, style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = goCalendar) { Text(strings.recentsSeeCalendarButton) }
                }
            }

            items(state.lastWorkouts) { w ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { goCalendar() }
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(w.title, fontWeight = FontWeight.SemiBold)
                        val info = strings.recentsItemInfo(
                            fmt.format(w.date),
                            w.sets,
                            w.reps
                        )
                        Text(
                            info,
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

@Composable
private fun StatCard(
    title: String,
    value: String,
    container: ColorScheme.() -> androidx.compose.ui.graphics.Color,
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
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MusclePreviewCard(
    onOpenMuscleMap: () -> Unit
) {
    val strings = LocalStrings.current.home

    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenMuscleMap() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.muscular),
                contentDescription = "Mapa muscular frontal y posterior",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = strings.musclePreviewTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = strings.musclePreviewHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Toca para abrir el mapa completo.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
