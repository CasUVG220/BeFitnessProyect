package com.befitnessapp.ui.screens.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.befitnessapp.domain.routines.RoutineDetail

@Composable
fun RoutineListScreen(
    routines: List<RoutineDetail>,
    onCreate: () -> Unit,
    onOpen: (RoutineDetail) -> Unit,
    onArchive: (Long) -> Unit
) {
    if (routines.isEmpty()) {
        Column(Modifier.padding(16.dp)) {
            Text("Aún no tienes rutinas guardadas.")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCreate) { Text("Crear rutina") }
        }
        return
    }

    LazyColumn(Modifier.padding(8.dp)) {
        items(routines) { r ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(r) }
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Text(r.routine.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text("${r.exercises.size} ejercicios", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { onArchive(r.routine.id) }) { Text("Archivar") }
            HorizontalDivider()
        }
    }
}
