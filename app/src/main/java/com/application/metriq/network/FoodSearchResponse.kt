package com.application.metriq.network

import com.google.gson.annotations.SerializedName

data class FoodSearchResponse(
    val foods: List<Food>
)

data class Food(
    val fdcId: Int,
    val description: String,
    val additionalDescriptions: String?,
    val brandName: String?,
    val foodNutrients: List<FoodNutrient>
)

data class FoodNutrient(
    val nutrientName: String,
    val value: Double,
    val unitName: String
)
