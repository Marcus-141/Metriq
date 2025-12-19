package com.application.metriq.data

import android.content.Context
import com.application.metriq.data.entity.ExerciseJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

object ExerciseTargetMapper {

    private val exerciseToFrontMuscles = mutableMapOf<String, List<FrontMuscleGroup>>()
    private val exerciseToRearMuscles = mutableMapOf<String, List<RearMuscleGroup>>()
    private var isLoaded = false

    fun loadExercises(context: Context) {
        if (isLoaded) return

        val jsonString: String
        try {
            jsonString = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return
        }

        val listExerciseType = object : TypeToken<List<ExerciseJson>>() {}.type
        val exercises: List<ExerciseJson> = Gson().fromJson(jsonString, listExerciseType)

        exercises.forEach { exercise ->
            // Combine primary and secondary muscles safely, handling nulls
            val targetMuscles = mutableListOf<String>()
            exercise.primaryMuscles?.let { targetMuscles.addAll(it) }
            exercise.secondaryMuscles?.let { targetMuscles.addAll(it) }

            val frontList = mutableListOf<FrontMuscleGroup>()
            val rearList = mutableListOf<RearMuscleGroup>()

            targetMuscles.forEach { targetName ->
                mapTargetToEnums(targetName, frontList, rearList)
            }

            if (frontList.isNotEmpty()) {
                exerciseToFrontMuscles[exercise.name] = frontList
            }
            if (rearList.isNotEmpty()) {
                exerciseToRearMuscles[exercise.name] = rearList
            }
        }
        isLoaded = true
    }

    fun getFrontTargets(exerciseName: String): List<FrontMuscleGroup> {
        return exerciseToFrontMuscles[exerciseName] ?: emptyList()
    }

    fun getRearTargets(exerciseName: String): List<RearMuscleGroup> {
        return exerciseToRearMuscles[exerciseName] ?: emptyList()
    }

    private fun mapTargetToEnums(
        target: String,
        frontList: MutableList<FrontMuscleGroup>,
        rearList: MutableList<RearMuscleGroup>
    ) {
        // Normalize string
        val key = target.lowercase().trim()

        when (key) {
            // Front Mappings
            "quads", "quadriceps" -> {
                frontList.add(FrontMuscleGroup.RIGHT_QUADS)
                frontList.add(FrontMuscleGroup.LEFT_QUADS)
            }
            "chest", "pectorals" -> {
                frontList.add(FrontMuscleGroup.PECS)
            }
            "abs", "abdominals" -> {
                frontList.add(FrontMuscleGroup.ABS)
            }
            "obliques" -> {
                frontList.add(FrontMuscleGroup.RIGHT_OBLIQUE)
                frontList.add(FrontMuscleGroup.LEFT_OBLIQUE)
            }
            "biceps" -> {
                frontList.add(FrontMuscleGroup.RIGHT_BICEP)
                frontList.add(FrontMuscleGroup.LEFT_BICEP)
            }
            "forearms" -> {
                frontList.add(FrontMuscleGroup.RIGHT_FOREARM_FRONT)
                frontList.add(FrontMuscleGroup.LEFT_FOREARM_FRONT)
                // Forearms are visible from rear too
                rearList.add(RearMuscleGroup.RIGHT_FOREARM_REAR)
                rearList.add(RearMuscleGroup.LEFT_FOREARM_REAR)
            }
            "front delts", "shoulders" -> { // Shoulders often implies front/side/rear, but primarily front visual
                 frontList.add(FrontMuscleGroup.RIGHT_DELTS_FRONT)
                 frontList.add(FrontMuscleGroup.LEFT_DELTS_FRONT)
            }
            "neck" -> {
                frontList.add(FrontMuscleGroup.NECK)
            }
            "adductors" -> {
                 // Often mapped to upper leg/quads area in simple maps or specific adductor group if available
                 // For now, mapping to Quads/Upper leg area if no specific adductor group
                 // Or skip if not in enum. Let's skip if not precise.
            }
            "serratus anterior" -> {
                frontList.add(FrontMuscleGroup.RIGHT_SERRATUS_ANTERIOR)
                frontList.add(FrontMuscleGroup.LEFT_SERRATUS_ANTERIOR)
            }
            "tibialis" -> {
                frontList.add(FrontMuscleGroup.RIGHT_CALF_FRONT)
                frontList.add(FrontMuscleGroup.LEFT_CALF_FRONT)
            }

            // Rear Mappings
            "back", "lats", "latissimus dorsi" -> {
                rearList.add(RearMuscleGroup.BACK)
            }
            "lower back" -> {
                 // If you have a lower back specific, use it. BACK usually covers lats. 
                 // If HIPS or similar covers lower back, map there.
                 // Often BACK is general.
                 rearList.add(RearMuscleGroup.BACK)
            }
            "traps", "trapezius" -> {
                rearList.add(RearMuscleGroup.TRAPS_REAR)
                frontList.add(FrontMuscleGroup.TRAPS_FRONT)
            }
            "glutes", "gluteus maximus" -> {
                rearList.add(RearMuscleGroup.GLUTES)
            }
            "hamstrings" -> {
                rearList.add(RearMuscleGroup.RIGHT_HAMSTRING)
                rearList.add(RearMuscleGroup.LEFT_HAMSTRING)
            }
            "calves", "gastrocnemius", "soleus" -> {
                rearList.add(RearMuscleGroup.RIGHT_CALF_REAR)
                rearList.add(RearMuscleGroup.LEFT_CALF_REAR)
            }
            "triceps" -> {
                rearList.add(RearMuscleGroup.RIGHT_TRICEPS_REAR)
                rearList.add(RearMuscleGroup.LEFT_TRICEPS_REAR)
                // Triceps visible from front too slightly?
                frontList.add(FrontMuscleGroup.RIGHT_TRICEPS_FRONT)
                frontList.add(FrontMuscleGroup.LEFT_TRICEPS_FRONT)
            }
            "rear delts" -> {
                rearList.add(RearMuscleGroup.RIGHT_DELTS_REAR)
                rearList.add(RearMuscleGroup.LEFT_DELTS_REAR)
            }
            // Add more as needed based on common JSON targets
        }
    }
}
