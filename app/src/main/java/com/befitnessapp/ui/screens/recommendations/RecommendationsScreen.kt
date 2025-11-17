package com.befitnessapp.ui.screens.recommendations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.recommendation.ExerciseScore
import com.befitnessapp.ui.localization.LocalStrings

@Composable
fun RecommendationsScreen(onBack: () -> Unit) {
    val vm: RecommendationsViewModel =
        viewModel(factory = RecommendationsViewModel.factory(Graph.workoutRepository))
    val list by vm.suggestions.collectAsState()

    val strings = LocalStrings.current.recommendations

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(strings.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            strings.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (list.isEmpty()) {
            ElevatedCard {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(strings.allCoveredTitle, fontWeight = FontWeight.SemiBold)
                    Text(strings.allCoveredText)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(strings.backButton)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(list) { s -> ExerciseSuggestionCard(s) }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(strings.backButton) }
                }
            }
        }
    }
}

@Composable
private fun ExerciseSuggestionCard(s: ExerciseScore) {
    val strings = LocalStrings.current.recommendations

    Card {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(s.name, style = MaterialTheme.typography.titleMedium)

            val formattedScore = formatScore(s.score)
            Text(
                strings.priorityLabel(formattedScore),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (s.deficits.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    s.deficits.entries
                        .sortedByDescending { it.value }
                        .forEach { (canonicalId, deficit) ->
                            val label = Catalogo.muscleById[canonicalId]?.name
                                ?: "Músculo $canonicalId"
                            val pct = (deficit * 100).toInt()
                            val chipText = strings.deficitChipLabel(label, pct)
                            SuggestionChip(chipText)
                        }
                }
            }
        }
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        Row(horizontalArrangement = horizontalArrangement) { content() }
    }
}

@Composable
private fun SuggestionChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private fun formatScore(v: Float): String {
    val s = String.format("%.2f", v)
    return if (s.endsWith("0")) s.trimEnd('0').trimEnd('.') else s
}
