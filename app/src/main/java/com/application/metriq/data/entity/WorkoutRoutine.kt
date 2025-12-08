package com.application.metriq.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.application.metriq.data.Converters

@Entity(tableName = "workout_routines")
@TypeConverters(Converters::class)
data class WorkoutRoutine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val exercises: List<String> = emptyList()
)
