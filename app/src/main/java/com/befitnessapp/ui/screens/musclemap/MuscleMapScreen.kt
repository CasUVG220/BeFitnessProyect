package com.befitnessapp.ui.screens.musclemap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.domain.catalog.Catalogo

@Composable
fun MuscleMapScreen(onBack: () -> Unit) {
    val vm: MuscleMapViewModel =
        viewModel(factory = MuscleMapViewModel.factory(Graph.workoutRepository))
    val coverage by vm.coverageByCanonical.collectAsState()

    // vistas (placeholder para cuando tengamos máscaras por frontal/posterior)
    var view by remember { mutableStateOf(BodyView.Front) }

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
                    Segmented(view = view, onChange = { view = it })
                }
            }
            Divider()

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1) “Silhouette” placeholder
                SilhouettePlaceholder(view = view)

                // 2) Heatmap por músculos canónicos
                Text("Cobertura (14 días)", style = MaterialTheme.typography.titleMedium)

                val musclesByGroup = remember(vm.canonicalMuscles) {
                    vm.canonicalMuscles.groupBy { it.groupId }
                }

                musclesByGroup.toSortedMap().forEach { (groupId, muscles) ->
                    val groupName = Catalogo.groupById[groupId]?.name ?: "Grupo $groupId"
                    Text(groupName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                    // filas de 3
                    val rows = muscles.chunked(3)
                    rows.forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { m ->
                                val cov = coverage[m.id] ?: 0f
                                HeatTile(
                                    label = m.name,
                                    coverage = cov,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Legend()

                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
            }
        }
    }
}

private enum class BodyView { Front, Back, Side }

@Composable
private fun Segmented(view: BodyView, onChange: (BodyView) -> Unit) {
    Row(
        Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SegBtn("Frente", view == BodyView.Front) { onChange(BodyView.Front) }
        SegBtn("Espalda", view == BodyView.Back) { onChange(BodyView.Back) }
        SegBtn("Perfil", view == BodyView.Side) { onChange(BodyView.Side) }
    }
}

@Composable
private fun SegBtn(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        tonalElevation = if (selected) 1.dp else 0.dp,
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SilhouettePlaceholder(view: BodyView) {
    // Aquí luego pegamos el overlay de máscaras por vista.
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        val label = when (view) {
            BodyView.Front -> "Vista frontal"
            BodyView.Back -> "Vista posterior"
            BodyView.Side -> "Vista lateral"
        }
        Box(Modifier.height(180.dp), contentAlignment = Alignment.Center) {
            Text("$label (placeholder)", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            val p = if (coverage.isFinite()) (coverage * 100).toInt() else 0
            val text = when {
                coverage < 1f -> "Cobertura: $p% (déficit)"
                else -> "Cobertura: $p%"
            }
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

//Cajas dependiendo a que % de progreso se a tenido en 7-14 dias para enfatizar el uso del algoritmo.
@Composable
private fun Legend() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LegendDot("0–40%", Color(0xFFD32F2F))     // rojo
        LegendDot("40–70%", Color(0xFFF57C00))    // naranja
        LegendDot("70–100%", Color(0xFFFBC02D))   // amarillo
        LegendDot("100–120%", Color(0xFF388E3C))  // verde
        LegendDot("120%+", Color(0xFF00897B))     // teal
    }
}

@Composable
private fun LegendDot(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = MaterialTheme.shapes.small
    ) { Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium) }
}

private fun coverageToColor(c: Float): Color {
    val v = when {
        c.isNaN() || c.isInfinite() -> 0f
        c < 0f -> 0f
        c > 1.4f -> 1.4f
        else -> c
    }
    return when {
        v < 0.4f -> lerp(Color(0xFFB71C1C), Color(0xFFF57C00), v / 0.4f)      // rojo → naranja
        v < 0.7f -> lerp(Color(0xFFF57C00), Color(0xFFFBC02D), (v - 0.4f)/0.3f) // naranja → amarillo
        v < 1.0f -> lerp(Color(0xFFFBC02D), Color(0xFF388E3C), (v - 0.7f)/0.3f) // amarillo → verde
        else     -> lerp(Color(0xFF388E3C), Color(0xFF00897B), (v - 1.0f)/0.4f) // verde → teal
    }
}

private fun lerp(a: Color, b: Color, tRaw: Float): Color {
    val t = tRaw.coerceIn(0f, 1f)
    return Color(
        red   = a.red   + (b.red   - a.red)   * t,
        green = a.green + (b.green - a.green) * t,
        blue  = a.blue  + (b.blue  - a.blue)  * t,
        alpha = 1f
    )
}
