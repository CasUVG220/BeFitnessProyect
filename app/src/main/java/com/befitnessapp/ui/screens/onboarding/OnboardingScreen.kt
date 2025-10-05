package com.befitnessapp.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.befitnessapp.ui.components.DotsIndicator

@Composable
fun OnboardingScreen(
    onTryDemo: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    // 5 mensajes + 1 slide final de acciones
    val messages = remember {
        listOf(
            "BeFitness",
            "Loggea tus workouts",
            "Aprende técnica con previews",
            "Equilibra tus músculos",
            "Recibe rutinas y sugerencias"
        )
    }
    val totalPages = messages.size + 1 // +1 para la página de CTAs

    val pagerState = rememberPagerState(pageCount = { totalPages })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Contenido deslizable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                if (page < messages.size) {
                    // Slides de texto
                    Text(
                        text = messages[page],
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                } else {
                    // Slide final (CTAs)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Empecemos!",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                        Button(onClick = onTryDemo, modifier = Modifier.fillMaxWidth()) {
                            Text("Probar demo")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                            Text("Entrar con Google")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
                            Text("Crear cuenta")
                        }
                    }
                }
            }
        }

        // Indicadores de página (elegantes, al fondo con padding)
        DotsIndicator(
            totalDots = totalPages,
            selectedIndex = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp) // “no tan” abajo, con margen
        )
    }
}
