package com.befitnessapp.ui.screens.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.auth.AuthState
import com.befitnessapp.ui.components.DotsIndicator
import com.befitnessapp.ui.screens.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// Move this to a more appropriate place if it's used elsewhere
private const val GOOGLE_WEB_CLIENT_ID =
    "480558685652-siiuk8r6a7m673bt7l3d0sh5j4mneue9.apps.googleusercontent.com"

@Composable
fun OnboardingScreen(
    onTryDemo: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                icon = Icons.Default.FitnessCenter,
                title = "BeFitness",
                subtitle = "Tu compañero de fitness definitivo para alcanzar todas tus metas."
            ),
            OnboardingPageData(
                icon = Icons.Default.ModelTraining,
                title = "Registra tus entrenamientos",
                subtitle = "Lleva un seguimiento detallado de cada serie, repetición y peso levantado."
            ),
            OnboardingPageData(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = "Visualiza tu progreso",
                subtitle = "Observa cómo evolucionan tus marcas y tu volumen a lo largo del tiempo."
            ),
            OnboardingPageData(
                icon = Icons.Default.VerifiedUser,
                title = "Equilibra tus músculos",
                subtitle = "Nuestro mapa muscular te ayuda a identificar y trabajar las áreas menos entrenadas."
            ),
            OnboardingPageData(
                icon = Icons.Default.AutoAwesome,
                title = "Rutinas inteligentes",
                subtitle = "Recibe recomendaciones personalizadas y crea tus propias rutinas."
            )
        )
    }
    val totalPages = pages.size + 1

    val pagerState = rememberPagerState(pageCount = { totalPages })

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(Graph.authRepository))
    val authState by authViewModel.authState.collectAsState(initial = AuthState.Loading)
    val context = LocalContext.current

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { authViewModel.loginWithGoogle(it) }
            } catch (_: ApiException) {
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.SignedIn) {
            onTryDemo() // Navigate to home if already logged in
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            if (page < pages.size) {
                OnboardingPageContent(data = pages[page])
            } else {
                ActionsPage(
                    onGoogleSignIn = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(GOOGLE_WEB_CLIENT_ID)
                            .requestEmail()
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        googleLauncher.launch(client.signInIntent)
                    },
                    onTryDemo = onTryDemo,
                    onLogin = onLogin,
                    onRegister = onRegister
                )
            }
        }

        DotsIndicator(
            totalDots = totalPages,
            selectedIndex = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}

private data class OnboardingPageData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
private fun OnboardingPageContent(data: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = data.icon,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(40.dp))
        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = data.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionsPage(
    onGoogleSignIn: () -> Unit,
    onTryDemo: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Empecemos!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Crea una cuenta para guardar tu progreso o prueba la app como invitado.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onGoogleSignIn,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar con Google")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onTryDemo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Probar como invitado")
        }

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Prefieres correo?")
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onLogin) { Text("Inicia sesión") }
            TextButton(onClick = onRegister) { Text("Regístrate") }
        }
    }
}
