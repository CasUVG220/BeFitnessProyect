package com.befitnessapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.befitnessapp.auth.AuthRepository
import com.befitnessapp.auth.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isBusy: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    val authState: Flow<AuthState> = repo.authState

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Opcional, pero útil si quieres refrescar usuario al abrir app
        viewModelScope.launch {
            runCatching { repo.refreshCurrentUser() }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(
                isBusy = false,
                errorMessage = "Completa correo y contraseña"
            )
            return
        }

        _uiState.value = AuthUiState(isBusy = true, errorMessage = null)

        viewModelScope.launch {
            try {
                repo.loginWithEmail(email.trim(), password)
                _uiState.value = AuthUiState(isBusy = false, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    isBusy = false,
                    errorMessage = e.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun registerWithEmail(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(
                isBusy = false,
                errorMessage = "Completa todos los campos"
            )
            return
        }

        _uiState.value = AuthUiState(isBusy = true, errorMessage = null)

        viewModelScope.launch {
            try {
                repo.registerWithEmail(name.trim(), email.trim(), password)
                _uiState.value = AuthUiState(isBusy = false, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    isBusy = false,
                    errorMessage = e.message ?: "Error al crear la cuenta"
                )
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        if (idToken.isBlank()) return
        _uiState.value = AuthUiState(isBusy = true, errorMessage = null)

        viewModelScope.launch {
            try {
                repo.loginWithGoogle(idToken)
                _uiState.value = AuthUiState(isBusy = false, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    isBusy = false,
                    errorMessage = e.message ?: "Error al iniciar con Google"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { repo.signOut() }
        }
    }

    companion object {
        fun factory(repo: AuthRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return AuthViewModel(repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
