package com.befitnessapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.befitnessapp.data.local.dao.WorkoutDao
import com.befitnessapp.data.local.entity.WorkoutEntity
import com.befitnessapp.data.local.entity.WorkoutSetEntity

@Database(
    entities = [WorkoutEntity::class, WorkoutSetEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}
