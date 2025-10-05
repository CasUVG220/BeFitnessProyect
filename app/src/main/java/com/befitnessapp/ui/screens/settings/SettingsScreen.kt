package com.befitnessapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ajustes – placeholder")
        // TODO: tema claro/oscuro/sistema, idioma, logout
        OutlinedButton(onClick = onBack) { Text("Volver") }
    }
}
