package com.befitnessapp.ui.screens.routines

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoutinesScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Rutinas – placeholder")
        // TODO: Builder/Preview/Mis Rutinas
        OutlinedButton(onClick = onBack) { Text("Volver") }
    }
}
