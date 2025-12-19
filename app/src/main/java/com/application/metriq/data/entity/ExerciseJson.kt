package com.application.metriq.data.entity

import com.google.gson.annotations.SerializedName

data class ExerciseJson(
    val name: String,
    val force: String?,
    val level: String?,
    val mechanic: String?,
    val equipment: String?,
    @SerializedName("primaryMuscles") val primaryMuscles: List<String>?,
    @SerializedName("secondaryMuscles") val secondaryMuscles: List<String>?,
    val instructions: List<String>?,
    val category: String?,
    val images: List<String>?,
    val id: String
)
