package com.befitnessapp.ui.screens.musclemap

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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.ui.localization.LocalStrings

@Composable
fun MuscleMapScreen(onBack: () -> Unit) {
    val vm: MuscleMapViewModel =
        viewModel(factory = MuscleMapViewModel.factory(Graph.workoutRepository))
    val coverage by vm.coverageByCanonical.collectAsState()

    var view by remember { mutableStateOf(BodyView.Front) }
    val strings = LocalStrings.current.muscleMap

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Surface(tonalElevation = 1.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onBack) { Text(strings.topBarBackButton) }
                    Spacer(Modifier.weight(1f))
                    Segmented(view = view, onChange = { view = it })
                }
            }
            HorizontalDivider()

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SilhouettePlaceholder(view = view)

                Text(strings.coverageTitle, style = MaterialTheme.typography.titleMedium)

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
                ) { Text(strings.fullBackButton) }
            }
        }
    }
}

private enum class BodyView { Front, Back, Side }

@Composable
private fun Segmented(view: BodyView, onChange: (BodyView) -> Unit) {
    val strings = LocalStrings.current.muscleMap

    Row(
        Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SegBtn(strings.segmentedFront, view == BodyView.Front) { onChange(BodyView.Front) }
        SegBtn(strings.segmentedBack, view == BodyView.Back) { onChange(BodyView.Back) }
        SegBtn(strings.segmentedSide, view == BodyView.Side) { onChange(BodyView.Side) }
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
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SilhouettePlaceholder(view: BodyView) {
    val strings = LocalStrings.current.muscleMap

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        val base = when (view) {
            BodyView.Front -> strings.silhouetteFront
            BodyView.Back -> strings.silhouetteBack
            BodyView.Side -> strings.silhouetteSide
        }
        Box(
            Modifier.height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$base ${strings.silhouettePlaceholderSuffix}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeatTile(label: String, coverage: Float, modifier: Modifier = Modifier) {
    val s = LocalStrings.current.muscleMap
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
                s.coverageDeficitLabel(p)
            } else {
                s.coverageLabel(p)
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
    val s = LocalStrings.current.muscleMap

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LegendDot(s.legend0to40, Color(0xFFD32F2F))
        LegendDot(s.legend40to70, Color(0xFFF57C00))
        LegendDot(s.legend70to100, Color(0xFFFBC02D))
        LegendDot(s.legend100to120, Color(0xFF388E3C))
        LegendDot(s.legend120Plus, Color(0xFF00897B))
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
