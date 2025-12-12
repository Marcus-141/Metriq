package com.application.metriq.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserProfile(
    val age: Int,
    val gender: String,
    val height: Double,
    val weight: Double,
    val activityLevel: String,
    val fitnessGoal: String
)

class UserPreferencesRepository(private val context: Context) {
    private val AGE = intPreferencesKey("age")
    private val GENDER = stringPreferencesKey("gender")
    private val HEIGHT = doublePreferencesKey("height")
    private val WEIGHT = doublePreferencesKey("weight")
    private val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
    private val FITNESS_GOAL = stringPreferencesKey("fitness_goal")

    val userProfile: Flow<UserProfile> = context.dataStore.data.map { preferences ->
        UserProfile(
            age = preferences[AGE] ?: 30,
            gender = preferences[GENDER] ?: "Male",
            height = preferences[HEIGHT] ?: 175.0,
            weight = preferences[WEIGHT] ?: 75.0,
            activityLevel = preferences[ACTIVITY_LEVEL] ?: "Sedentary",
            fitnessGoal = preferences[FITNESS_GOAL] ?: "Maintain Weight"
        )
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[AGE] = profile.age
            preferences[GENDER] = profile.gender
            preferences[HEIGHT] = profile.height
            preferences[WEIGHT] = profile.weight
            preferences[ACTIVITY_LEVEL] = profile.activityLevel
            preferences[FITNESS_GOAL] = profile.fitnessGoal
        }
    }
}
