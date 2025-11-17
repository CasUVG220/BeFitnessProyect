package com.befitnessapp

import android.content.Context
import com.befitnessapp.auth.AuthRepository
import com.befitnessapp.auth.FirebaseAuthRepository
import com.befitnessapp.data.local.db.DatabaseProvider
import com.befitnessapp.data.prefs.UserPrefs
import com.befitnessapp.data.repository.WorkoutRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object Graph {

    @Volatile
    private var initialized = false

    lateinit var workoutRepository: WorkoutRepository
        private set

    lateinit var userPrefs: UserPrefs
        private set

    lateinit var authRepository: AuthRepository
        private set

    fun init(context: Context) {
        if (initialized) return

        val appContext = context.applicationContext

        // --- Room (workouts) ---
        val db = DatabaseProvider.get(appContext)
        workoutRepository = WorkoutRepository(db.workoutDao())

        // --- Preferencias locales ---
        userPrefs = UserPrefs(appContext)

        // --- Firebase (Auth + Firestore) ---
        FirebaseApp.initializeApp(appContext)

        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        authRepository = FirebaseAuthRepository(
            firebaseAuth = firebaseAuth,
            firestore = firestore
        )

        initialized = true
    }
}
