package com.befitnessapp

import android.content.Context
import com.befitnessapp.data.local.db.DatabaseProvider
import com.befitnessapp.data.prefs.UserPrefs
import com.befitnessapp.data.repository.WorkoutRepository

object Graph {
    @Volatile private var initialized = false

    lateinit var workoutRepository: WorkoutRepository
        private set

    lateinit var userPrefs: UserPrefs
        private set

    fun init(context: Context) {
        if (initialized) return
        val db = DatabaseProvider.get(context)
        workoutRepository = WorkoutRepository(db.workoutDao())
        userPrefs = UserPrefs(context.applicationContext)
        initialized = true
    }
}
