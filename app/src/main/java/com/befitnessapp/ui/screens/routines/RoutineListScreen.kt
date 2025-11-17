package com.befitnessapp.ui.screens.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.befitnessapp.domain.routines.RoutineDetail
import com.befitnessapp.ui.localization.LocalStrings

@Composable
fun RoutineListScreen(
    routines: List<RoutineDetail>,
    onCreate: () -> Unit,
    onOpen: (RoutineDetail) -> Unit,
    onArchive: (Long) -> Unit
) {
    val strings = LocalStrings.current

    if (routines.isEmpty()) {
        EmptyState(onCreate = onCreate)
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(routines) { r ->
            RoutineRow(
                detail = r,
                onOpen = { onOpen(r) },
                onArchive = { r.routine.id?.let(onArchive) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun EmptyState(onCreate: () -> Unit) {
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(strings.routines.savedEmptyTitle)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onCreate) {
            Text(strings.routines.savedEmptyCreateButton)
        }
    }
}

@Composable
private fun RoutineRow(
    detail: RoutineDetail,
    onOpen: () -> Unit,
    onArchive: () -> Unit
) {
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = detail.routine.name.ifBlank { strings.routines.listCardFallbackName },
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${detail.exercises.size} ${strings.routines.listCardExercisesLabel}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onOpen) {
                Text(strings.routines.savedRowOpenButton)
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onArchive) {
                Text(strings.routines.savedRowArchiveButton)
            }
        }
    }
}
