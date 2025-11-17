package com.befitnessapp.auth

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?
)

sealed class AuthState {
    object Loading : AuthState()
    object SignedOut : AuthState()
    data class SignedIn(val user: AuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
