package com.application.metriq.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromRoutineExerciseList(value: String?): List<RoutineExercise> {
        if (value == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<RoutineExercise>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toRoutineExerciseList(list: List<RoutineExercise>): String {
        return gson.toJson(list)
    }
}
