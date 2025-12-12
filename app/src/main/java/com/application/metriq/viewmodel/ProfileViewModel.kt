package com.application.metriq.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.application.metriq.data.UserPreferencesRepository
import com.application.metriq.data.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserGoals(
    val calorieGoal: Double,
    val proteinGoal: Double,
    val carbsGoal: Double,
    val fatsGoal: Double
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserPreferencesRepository(application)

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile(30, "Male", 175.0, 75.0, "Sedentary", "Maintain Weight"))

    val userGoals: StateFlow<UserGoals> = userProfile.map { profile ->
        calculateGoals(profile)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserGoals(2000.0, 150.0, 200.0, 67.0))

    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateUserProfile(profile)
        }
    }

    private fun calculateGoals(profile: UserProfile): UserGoals {
        // Mifflin-St Jeor Equation
        // Men: 10 * weight (kg) + 6.25 * height (cm) - 5 * age (y) + 5
        // Women: 10 * weight (kg) + 6.25 * height (cm) - 5 * age (y) - 161
        
        var bmr = 10 * profile.weight + 6.25 * profile.height - 5 * profile.age
        bmr += if (profile.gender.equals("Male", ignoreCase = true)) 5 else -161

        // Activity Multiplier
        val activityMultiplier = when (profile.activityLevel) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Very Active" -> 1.725
            "Extra Active" -> 1.9
            else -> 1.2
        }

        val tdee = bmr * activityMultiplier

        // Goal Adjustment
        val calorieGoal = when (profile.fitnessGoal) {
            "Lose Fat" -> tdee - 500
            "Gain Muscle" -> tdee + 300
            else -> tdee // Maintain Weight
        }

        // Macro Distribution (Example: 30% Protein, 40% Carbs, 30% Fats)
        // Protein: 4 cal/g, Carbs: 4 cal/g, Fats: 9 cal/g
        
        val proteinGoal = (calorieGoal * 0.30) / 4
        val carbsGoal = (calorieGoal * 0.40) / 4
        val fatsGoal = (calorieGoal * 0.30) / 9

        return UserGoals(
            calorieGoal = calorieGoal.coerceAtLeast(1200.0), // Minimum safety buffer
            proteinGoal = proteinGoal,
            carbsGoal = carbsGoal,
            fatsGoal = fatsGoal
        )
    }
}
