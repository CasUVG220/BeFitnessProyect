package com.befitnessapp.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: Flow<AuthState>
        get() = _authState.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                _authState.value = AuthState.SignedOut
            } else {
                _authState.value = AuthState.SignedIn(
                    AuthUser(
                        uid = user.uid,
                        email = user.email,
                        displayName = user.displayName
                    )
                )
            }
        }
    }

    override suspend fun refreshCurrentUser() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            user.reload().await()
        }
        // El listener actualizará _authState si algo cambia
    }

    override suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String
    ) {
        val result = firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .await()

        val user = result.user ?: throw IllegalStateException("User is null after register")

        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(profileUpdate).await()

        val doc = mapOf(
            "uid" to user.uid,
            "name" to (user.displayName ?: name),
            "email" to (user.email ?: email),
            "authSource" to "email_password"
        )

        firestore.collection("users")
            .document(user.uid)
            .set(doc)
            .await()
    }

    override suspend fun loginWithEmail(
        email: String,
        password: String
    ) {
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()
        // El listener de auth actualizará _authState
    }

    override suspend fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth
            .signInWithCredential(credential)
            .await()

        val user = result.user ?: throw IllegalStateException("User is null after Google login")

        val doc = mapOf(
            "uid" to user.uid,
            "name" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "authSource" to "google"
        )

        firestore.collection("users")
            .document(user.uid)
            .set(doc)
            .await()
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        _authState.value = AuthState.SignedOut
    }
}

private suspend fun <T> Task<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                @Suppress("UNCHECKED_CAST")
                cont.resume(task.result as T)
            } else {
                val e = task.exception ?: Exception("Firebase task failed")
                cont.resumeWithException(e)
            }
        }
        cont.invokeOnCancellation {
            // no-op
        }
    }
