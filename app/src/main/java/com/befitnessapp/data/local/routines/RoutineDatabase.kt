package com.befitnessapp.data.local.routines

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        RoutineSetTemplateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RoutineDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao

    companion object {
        @Volatile private var INSTANCE: RoutineDatabase? = null
        fun get(context: Context): RoutineDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RoutineDatabase::class.java,
                    "routines.db"
                ).build().also { INSTANCE = it }
            }
    }
}
