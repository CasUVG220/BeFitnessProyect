package com.befitnessapp.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val authState: Flow<AuthState>

    suspend fun refreshCurrentUser()

    suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String
    )

    suspend fun loginWithEmail(
        email: String,
        password: String
    )

    suspend fun loginWithGoogle(idToken: String)

    suspend fun signOut()
}
