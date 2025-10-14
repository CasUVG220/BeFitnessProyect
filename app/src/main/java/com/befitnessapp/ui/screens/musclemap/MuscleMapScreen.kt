package com.befitnessapp.ui.screens.musclemap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MuscleMapScreen(onBack: () -> Unit) {
    val groups = remember {
        listOf(
            "Pecho", "Espalda", "Hombros", "Trapecio", "Core", "Antebrazo",
            "Bíceps", "Tríceps", "Glúteo", "Cuádriceps", "Isquios", "Pantorrilla"
        )
    }
    val selected = remember { mutableStateListOf<String>() }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Surface(tonalElevation = 1.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) { Text("Atrás") }
                    Spacer(Modifier.weight(1f))
                    Text("Mapa muscular", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                }
            }
            Divider()

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // “Silhouette” placeholder
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(Modifier.height(180.dp), contentAlignment = Alignment.Center) {
                        Text("Vista frontal / posterior (placeholder)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text("Grupos musculares", style = MaterialTheme.typography.titleMedium)

                val rows = groups.chunked(3)
                rows.forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { name ->
                            val isSel = name in selected
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    if (isSel) selected.remove(name) else selected.add(name)
                                },
                                label = { Text(name) }
                            )
                        }
                    }
                }

                // Resumen de selección (solo visual)
                if (selected.isNotEmpty()) {
                    ElevatedCard {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Selección actual", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(selected.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = { /* en el futuro: navegar a Biblioteca filtrada */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Explorar ejercicios (próximamente)")
                }
            }
        }
    }
}
