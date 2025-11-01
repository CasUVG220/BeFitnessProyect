package com.befitnessapp.data.local.routines

import androidx.room.*
import java.time.Instant

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val updatedAt: Long = Instant.now().toEpochMilli(),
    val isArchived: Boolean = false
)

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId"), Index("exerciseId")]
)
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val routineId: Long,
    val exerciseId: Int,      // usa IDs del catálogo actual
    val displayName: String,
    val orderIndex: Int
)

@Entity(
    tableName = "routine_set_templates",
    foreignKeys = [
        ForeignKey(
            entity = RoutineExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineExerciseId")]
)
data class RoutineSetTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val routineExerciseId: Long,
    val indexInExercise: Int,
    val reps: Int,
    val weight: Float
)

data class RoutineExerciseWithSets(
    @Embedded val exercise: RoutineExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineExerciseId",
        entity = RoutineSetTemplateEntity::class
    )
    val sets: List<RoutineSetTemplateEntity>
)

data class RoutineWithContent(
    @Embedded val routine: RoutineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId",
        entity = RoutineExerciseEntity::class
    )
    val exercises: List<RoutineExerciseWithSets>
)
