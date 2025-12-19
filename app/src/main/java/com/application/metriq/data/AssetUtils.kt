package com.application.metriq.data

import android.content.Context
import android.util.Log
import com.application.metriq.data.entity.ExerciseJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

fun loadExercisesFromAssets(context: Context?): List<ExerciseJson> {
    if (context == null) {
        Log.e("AssetUtils", "Context is null, cannot load exercises.")
        return emptyList()
    }

    return try {
        val jsonString = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<ExerciseJson>>() {}.type
        Gson().fromJson(jsonString, listType) ?: emptyList()
    } catch (e: IOException) {
        Log.e("AssetUtils", "IOException: Couldn't read exercises.json from assets", e)
        emptyList()
    } catch (e: Exception) {
        Log.e("AssetUtils", "An unexpected error occurred during asset loading", e)
        emptyList()
    }
}
