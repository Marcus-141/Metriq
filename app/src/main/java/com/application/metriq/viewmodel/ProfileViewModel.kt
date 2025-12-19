package com.application.metriq.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.application.metriq.data.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserProfile(
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val gender: String = "",
    val activityLevel: String = "",
    val fitnessGoal: String = "",
    val calorieGoal: String = ""
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)

    val userProfile: StateFlow<UserProfile> = combine(
        sessionManager.weight,
        sessionManager.height,
        sessionManager.age,
        sessionManager.gender,
        sessionManager.activityLevel,
        sessionManager.fitnessGoal,
        sessionManager.calorieGoal
    ) { args: Array<*> ->
        val weight = args[0] as String
        val height = args[1] as String
        val age = args[2] as String
        val gender = args[3] as String
        val activityLevel = args[4] as String
        val fitnessGoal = args[5] as String
        val calorieGoal = args[6] as String

        UserProfile(weight, height, age, gender, activityLevel, fitnessGoal, calorieGoal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val userGoals: StateFlow<UserGoals> = combine(
        sessionManager.calorieGoal,
        sessionManager.protein,
        sessionManager.carbs,
        sessionManager.fats
    ) { args: Array<*> ->
        val calorieGoalStr = args[0] as? String
        val proteinGoalStr = args[1] as? String
        val carbsGoalStr = args[2] as? String
        val fatsGoalStr = args[3] as? String

        val calorieGoal = if (calorieGoalStr.isNullOrBlank()) 2000.0 else calorieGoalStr.toDoubleOrNull() ?: 2000.0
        val proteinGoal = if (proteinGoalStr.isNullOrBlank()) 150.0 else proteinGoalStr.toDoubleOrNull() ?: 150.0
        val carbsGoal = if (carbsGoalStr.isNullOrBlank()) 200.0 else carbsGoalStr.toDoubleOrNull() ?: 200.0
        val fatsGoal = if (fatsGoalStr.isNullOrBlank()) 67.0 else fatsGoalStr.toDoubleOrNull() ?: 67.0

        UserGoals(
            calorieGoal = calorieGoal,
            proteinGoal = proteinGoal,
            carbsGoal = carbsGoal,
            fatsGoal = fatsGoal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserGoals(2000.0, 150.0, 200.0, 67.0))


    fun saveProfileAndCalculateGoals(profile: UserProfile) {
        viewModelScope.launch {
            val weightVal = profile.weight.toDoubleOrNull() ?: 0.0
            val heightVal = profile.height.toDoubleOrNull() ?: 0.0
            val ageVal = profile.age.toIntOrNull() ?: 0
            
            // Calculate BMR using Mifflin-St Jeor
            // Men: 10W + 6.25H - 5A + 5
            // Women: 10W + 6.25H - 5A - 161
            var bmr = (10 * weightVal) + (6.25 * heightVal) - (5 * ageVal)
            bmr += if (profile.gender.equals("Male", ignoreCase = true)) 5 else -161

            // Determine Activity Factor
            val activityFactor = when (profile.activityLevel) {
                "Sedentary" -> 1.2
                "Lightly Active" -> 1.375
                "Moderately Active" -> 1.55
                "Very Active" -> 1.725
                "Extra Active" -> 1.9
                else -> 1.2
            }

            val tdee = bmr * activityFactor

            // Adjust for Fitness Goal
            val targetCalories = when (profile.fitnessGoal) {
                "Lose Fat" -> tdee - 500
                "Gain Muscle" -> tdee + 300
                else -> tdee // Maintain Weight
            }

            // Ensure calories don't drop below safety threshold (e.g., 1200)
            val finalCalories = targetCalories.coerceAtLeast(1200.0)
            
            // Macro Logic: Protein First Method
            // 1. Protein: Based on body weight (e.g., 2.0g per kg)
            // Note: Common range is 1.6-2.2g/kg for active individuals. 
            // We'll use 2.0g/kg as a solid baseline for body composition.
            // If the user is very overweight, lean body mass would be better, but total weight is simpler for now.
            // Cap protein at a reasonable max (e.g., 250g) to avoid extreme values for very heavy users.
            val proteinGoal = (weightVal * 2.0).coerceAtMost(250.0)
            val proteinCalories = proteinGoal * 4

            // 2. Fats: Based on body weight (e.g., 0.9g per kg)
            // Healthy range often cited as 0.8-1.0g/kg.
            val fatsGoal = (weightVal * 0.9).coerceAtMost(100.0)
            val fatsCalories = fatsGoal * 9

            // 3. Carbs: Remaining calories
            val remainingCalories = finalCalories - proteinCalories - fatsCalories
            // Ensure carbs don't go negative or too low (minimum safety buffer)
            val carbsCalories = remainingCalories.coerceAtLeast(0.0)
            val carbsGoal = carbsCalories / 4

            sessionManager.saveProfileData(
                weight = profile.weight,
                height = profile.height,
                age = profile.age,
                gender = profile.gender,
                activityLevel = profile.activityLevel,
                fitnessGoal = profile.fitnessGoal,
                calorieGoal = finalCalories.toInt().toString(),
                protein = proteinGoal.toInt().toString(),
                carbs = carbsGoal.toInt().toString(),
                fats = fatsGoal.toInt().toString(),
                bmr = bmr.toInt().toString(),
                tee = tdee.toInt().toString()
            )
        }
    }
}
