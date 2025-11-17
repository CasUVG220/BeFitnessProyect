package com.befitnessapp.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.ui.localization.LocalStrings

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val vm: ProfileViewModel =
        viewModel(factory = ProfileViewModel.factory(Graph.workoutRepository))
    val state by vm.uiState.collectAsState()
    val strings = LocalStrings.current.profile

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.backButtonContentDescription
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccountCard(strings)

            if (state.totalWorkouts == 0) {
                EmptyStatsCard(strings)
            } else {
                SummaryCard(strings, state)
                StreaksCard(strings, state)
            }
        }
    }
}

@Composable
private fun AccountCard(strings: com.befitnessapp.ui.localization.ProfileStrings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = strings.accountSectionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(strings.accountNameLabel, style = MaterialTheme.typography.bodyMedium)
            Text(strings.accountEmailLabel, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = strings.accountSourceLabel,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun EmptyStatsCard(strings: com.befitnessapp.ui.localization.ProfileStrings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = strings.emptyStatsTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = strings.emptyStatsText,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = strings.emptyStatsHint,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SummaryCard(
    strings: com.befitnessapp.ui.localization.ProfileStrings,
    state: ProfileUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = strings.summaryTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileStatCard(
                    title = strings.totalWorkoutsLabel,
                    value = state.totalWorkouts.toString(),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = strings.totalVolumeLabel,
                    value = "${state.totalVolume.toInt()} kg·rep",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileStatCard(
                    title = strings.totalSetsLabel,
                    value = state.totalSets.toString(),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = strings.totalRepsLabel,
                    value = state.totalReps.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StreaksCard(
    strings: com.befitnessapp.ui.localization.ProfileStrings,
    state: ProfileUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = strings.streaksTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileStatCard(
                    title = strings.bestStreakLabel,
                    value = state.bestStreak.toString(),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    title = strings.currentStreakLabel,
                    value = state.currentStreak.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = strings.streaksInfo,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
