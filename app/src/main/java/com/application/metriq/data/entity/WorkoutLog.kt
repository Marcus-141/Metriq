package com.application.metriq.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.application.metriq.data.Converters
import com.application.metriq.data.RoutineExercise

@Entity(tableName = "workout_logs")
@TypeConverters(Converters::class)
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Added name
    val timestamp: Long,
    val duration: Long, // in seconds
    val exercises: List<RoutineExercise>
)
