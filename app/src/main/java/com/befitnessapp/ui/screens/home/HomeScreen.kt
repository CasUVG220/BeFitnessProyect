package com.befitnessapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    goDashboard: () -> Unit,
    goLibrary: () -> Unit,
    goMuscleMap: () -> Unit,
    goLog: () -> Unit,
    goAddLog: () -> Unit,
    goRecommendations: () -> Unit,
    goRoutines: () -> Unit,
    goCalendar: () -> Unit,
    goProfile: () -> Unit,
    goSettings: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Home (hub)")
        Button(onClick = goDashboard, modifier = Modifier.fillMaxWidth()) { Text("Dashboard") }
        Button(onClick = goLibrary, modifier = Modifier.fillMaxWidth()) { Text("Biblioteca de ejercicios") }
        Button(onClick = goMuscleMap, modifier = Modifier.fillMaxWidth()) { Text("Mapa muscular") }
        Button(onClick = goLog, modifier = Modifier.fillMaxWidth()) { Text("Historial de entrenos") }
        Button(onClick = goAddLog, modifier = Modifier.fillMaxWidth()) { Text("Agregar entrenamiento") }
        Button(onClick = goRecommendations, modifier = Modifier.fillMaxWidth()) { Text("Recomendaciones") }
        Button(onClick = goRoutines, modifier = Modifier.fillMaxWidth()) { Text("Rutinas") }
        Button(onClick = goCalendar, modifier = Modifier.fillMaxWidth()) { Text("Calendario") }
        Button(onClick = goProfile, modifier = Modifier.fillMaxWidth()) { Text("Perfil") }
        Button(onClick = goSettings, modifier = Modifier.fillMaxWidth()) { Text("Ajustes") }
    }
}
