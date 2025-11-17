package com.befitnessapp.ui.screens.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.befitnessapp.Graph
import com.befitnessapp.auth.AuthState
import com.befitnessapp.auth.GOOGLE_WEB_CLIENT_ID
import com.befitnessapp.ui.components.DotsIndicator
import com.befitnessapp.ui.screens.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun OnboardingScreen(
    onTryDemo: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    val messages = remember {
        listOf(
            "BeFitness",
            "Loggea tus workouts",
            "Aprende técnica con previews",
            "Equilibra tus músculos",
            "Recibe rutinas y sugerencias"
        )
    }
    val totalPages = messages.size + 1

    val pagerState = rememberPagerState(pageCount = { totalPages })

    // Auth: para saber si ya hay usuario loggeado y hacer login con Google
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(Graph.authRepository)
    )
    val authState by authViewModel.authState.collectAsState(initial = AuthState.Loading)

    val context = LocalContext.current

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    authViewModel.loginWithGoogle(idToken)
                }
            } catch (_: ApiException) {
            }
        }
    }

    // Si ya hay usuario autenticado, saltamos Onboarding → Home
    LaunchedEffect(authState) {
        if (authState is AuthState.SignedIn) {
            onTryDemo()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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
                    Text(
                        text = messages[page],
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                } else {
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
                        Button(
                            onClick = onTryDemo,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Probar demo")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val gso = GoogleSignInOptions.Builder(
                                    GoogleSignInOptions.DEFAULT_SIGN_IN
                                )
                                    .requestIdToken(GOOGLE_WEB_CLIENT_ID)
                                    .requestEmail()
                                    .build()

                                val client = GoogleSignIn.getClient(context, gso)
                                googleLauncher.launch(client.signInIntent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Iniciar sesión con Google")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onLogin,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Iniciar sesión con correo")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onRegister,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Crear cuenta")
                        }
                    }
                }
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
