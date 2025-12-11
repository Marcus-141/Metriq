package com.application.metriq.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logged_foods")
data class LoggedFood(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val weight: Double,
    val timestamp: Long
)
