package com.application.metriq.data

import android.content.Context
import com.application.metriq.data.entity.ExerciseJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun loadExercisesFromAssets(context: Context): List<ExerciseJson> {
    return try {
        val jsonString = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<ExerciseJson>>() {}.type
        Gson().fromJson(jsonString, listType)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
