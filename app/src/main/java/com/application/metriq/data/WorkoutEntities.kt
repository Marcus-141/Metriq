package com.application.metriq.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

// Defines a single set within an exercise (e.g., 10 reps at 50kg)
data class ExerciseSet(
    var reps: String = "",
    var weight: String = "",
    var isCompleted: Boolean = false
)

// Defines an exercise within a routine (e.g., Bench Press) and its list of sets
data class RoutineExercise(
    val name: String,
    val sets: MutableList<ExerciseSet> = mutableListOf(ExerciseSet())
)

// Defines the user-created workout routine template
@Entity(tableName = "workout_routines")
@TypeConverters(Converters::class)
data class WorkoutRoutine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val exercises: List<RoutineExercise> = emptyList()
)
