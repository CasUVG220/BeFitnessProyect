package com.befitnessapp.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Dashboard (KPIs y tendencias) – placeholder")
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) { Text("Volver") }
    }
}
