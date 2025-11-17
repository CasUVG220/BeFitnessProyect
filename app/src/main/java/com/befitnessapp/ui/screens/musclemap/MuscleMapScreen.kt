package com.befitnessapp.ui.screens.musclemap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.R
import com.befitnessapp.domain.catalog.Catalogo

@Composable
fun MuscleMapScreen(onBack: () -> Unit) {
    val vm: MuscleMapViewModel =
        viewModel(factory = MuscleMapViewModel.factory(Graph.workoutRepository))
    val coverage by vm.coverageByCanonical.collectAsState()

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Surface(tonalElevation = 1.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onBack) { Text("Atrás") }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Mapa muscular",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            HorizontalDivider()

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BodyMapCard()

                Text("Cobertura (14 días)", style = MaterialTheme.typography.titleMedium)

                val musclesByGroup = remember(vm.canonicalMuscles) {
                    vm.canonicalMuscles.groupBy { it.groupId }
                }

                musclesByGroup.toSortedMap().forEach { (groupId, muscles) ->
                    val groupName = Catalogo.groupById[groupId]?.name ?: "Grupo $groupId"
                    Text(
                        groupName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    val rows = muscles.chunked(3)
                    rows.forEach { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { m ->
                                val cov = coverage[m.id] ?: 0f
                                HeatTile(
                                    label = m.name,
                                    coverage = cov,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size < 3) repeat(3 - row.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Legend()

                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Volver") }
            }
        }
    }
}

@Composable
private fun BodyMapCard() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.muscular),
                contentDescription = "Mapa muscular frontal y posterior",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                "Cada color representa un grupo muscular (pecho, espalda, hombros, brazos, piernas, core, etc.).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MuscleGroupLegend()
        }
    }
}

@Composable
private fun MuscleGroupLegend() {
    val items = listOf(
        "Pecho" to Color(0xFF1976D2),        // azul
        "Espalda" to Color(0xFFEF5350),      // rojo
        "Hombros" to Color(0xFFFFA726),     // naranja
        "Bíceps" to Color(0xFF29B6F6),      // celeste
        "Tríceps" to Color(0xFFAB47BC),     // púrpura
        "Antebrazo" to Color(0xFFFFCDD2),   // rosado claro
        "Core" to Color(0xFFFFEB3B),        // amarillo
        "Glúteo" to Color(0xFF66BB6A),      // verde
        "Cuádriceps" to Color(0xFF8BC34A),  // verde claro
        "Isquios" to Color(0xFFFF7043),     // naranja quemado
        "Pantorrilla" to Color(0xFFEC407A), // rosa fuerte
        "Trapecio" to Color(0xFF5C6BC0)     // azul violáceo
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Leyenda por color",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        items.chunked(3).forEach { rowItems ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { (label, color) ->
                    LegendDotWithLabel(
                        label = label,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size < 3) repeat(3 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LegendDotWithLabel(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun HeatTile(label: String, coverage: Float, modifier: Modifier = Modifier) {
    val color = coverageToColor(coverage)
    Surface(
        color = color.copy(alpha = 0.18f),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            val p = if (coverage.isFinite()) (coverage * 100).toInt() else 0
            val text = if (coverage < 1f) {
                "Cobertura: $p% (déficit)"
            } else {
                "Cobertura: $p%"
            }
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Legend() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LegendDot("0–40%", Color(0xFFD32F2F))
        LegendDot("40–70%", Color(0xFFF57C00))
        LegendDot("70–100%", Color(0xFFFBC02D))
        LegendDot("100–120%", Color(0xFF388E3C))
        LegendDot("120%+", Color(0xFF00897B))
    }
}

@Composable
private fun LegendDot(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun coverageToColor(c: Float): Color {
    val v = when {
        c.isNaN() || c.isInfinite() -> 0f
        c < 0f -> 0f
        c > 1.4f -> 1.4f
        else -> c
    }
    return when {
        v < 0.4f -> lerp(Color(0xFFB71C1C), Color(0xFFF57C00), v / 0.4f)
        v < 0.7f -> lerp(Color(0xFFF57C00), Color(0xFFFBC02D), (v - 0.4f) / 0.3f)
        v < 1.0f -> lerp(Color(0xFFFBC02D), Color(0xFF388E3C), (v - 0.7f) / 0.3f)
        else -> lerp(Color(0xFF388E3C), Color(0xFF00897B), (v - 1.0f) / 0.4f)
    }
}

private fun lerp(a: Color, b: Color, tRaw: Float): Color {
    val t = tRaw.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * t,
        green = a.green + (b.green - a.green) * t,
        blue = a.blue + (b.blue - a.blue) * t,
        alpha = 1f
    )
}
