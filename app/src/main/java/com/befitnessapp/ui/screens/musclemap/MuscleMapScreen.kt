package com.befitnessapp.ui.screens.musclemap

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MuscleMapScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Mapa muscular (SVG) – placeholder")
        // TODO: Render del SVG frontal/dorsal
        OutlinedButton(onClick = onBack) { Text("Volver") }
    }
}
