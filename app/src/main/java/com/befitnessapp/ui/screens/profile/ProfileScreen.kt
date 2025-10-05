package com.befitnessapp.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Perfil – placeholder")
        // TODO: objetivo, días/semana, kg/lb, equipo disponible
        OutlinedButton(onClick = onBack) { Text("Volver") }
    }
}
