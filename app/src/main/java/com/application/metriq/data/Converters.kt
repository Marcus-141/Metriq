package com.application.metriq.data

import androidx.room.TypeConverter
import com.application.metriq.data.entity.RoutineExercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromRoutineExerciseList(value: String?): MutableList<RoutineExercise> {
        if (value == null) {
            return mutableListOf()
        }
        val listType = object : TypeToken<MutableList<RoutineExercise>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toRoutineExerciseList(list: MutableList<RoutineExercise>): String {
        return gson.toJson(list)
    }
}
