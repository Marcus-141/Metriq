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
    
    // Vitamins
    val vitaminD: Double = 0.0,
    val vitaminB12: Double = 0.0,
    val folate: Double = 0.0,
    val vitaminA: Double = 0.0,
    val vitaminC: Double = 0.0,
    
    // Minerals
    val iron: Double = 0.0,
    val calcium: Double = 0.0,
    val magnesium: Double = 0.0,
    val iodine: Double = 0.0,
    val zinc: Double = 0.0,
    
    // Other Micronutrients
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0,
    val cholesterol: Double = 0.0,
    val potassium: Double = 0.0,
    val saturatedFat: Double = 0.0,
    
    val mealType: String = MealType.SNACK.name,

    val weight: Double,
    val timestamp: Long
)
