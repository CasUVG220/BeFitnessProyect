package com.befitnessapp.ui.screens.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.befitnessapp.domain.routines.RoutineDetail

@Composable
fun RoutinePickerDialog(
    routines: List<RoutineDetail>,
    onPick: (RoutineDetail) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona una rutina") },
        text = {
            if (routines.isEmpty()) {
                Text("No tienes rutinas activas.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(routines) { r ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(r) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(r.routine.name, style = MaterialTheme.typography.titleMedium)
                            Text("${r.exercises.size} ejercicios", style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}
