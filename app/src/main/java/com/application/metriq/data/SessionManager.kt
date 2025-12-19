package com.application.metriq.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// CHANGE 1: Rename this to something specific to avoid conflicts
private val Context.sessionDataStore by preferencesDataStore(name = "session")

class SessionManager(context: Context) {

    // CHANGE 2: Update the reference here to match the name above
    private val dataStore = context.sessionDataStore

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val WEIGHT = stringPreferencesKey("weight")
        val HEIGHT = stringPreferencesKey("height")
        val AGE = stringPreferencesKey("age")
        val GENDER = stringPreferencesKey("gender")
        val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        val FITNESS_GOAL = stringPreferencesKey("fitness_goal")
        val CALORIE_GOAL = stringPreferencesKey("calorie_goal")
        val PROTEIN = stringPreferencesKey("protein")
        val CARBS = stringPreferencesKey("carbs")
        val FATS = stringPreferencesKey("fats")
        val BMR = stringPreferencesKey("bmr")
        val TEE = stringPreferencesKey("tee")
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit {
            it[IS_LOGGED_IN] = isLoggedIn
        }
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map {
        it[IS_LOGGED_IN] ?: false
    }

    suspend fun saveProfileData(
        weight: String,
        height: String,
        age: String,
        gender: String,
        activityLevel: String,
        fitnessGoal: String,
        calorieGoal: String,
        protein: String,
        carbs: String,
        fats: String,
        bmr: String,
        tee: String
    ) {
        dataStore.edit {
            it[WEIGHT] = weight
            it[HEIGHT] = height
            it[AGE] = age
            it[GENDER] = gender
            it[ACTIVITY_LEVEL] = activityLevel
            it[FITNESS_GOAL] = fitnessGoal
            it[CALORIE_GOAL] = calorieGoal
            it[PROTEIN] = protein
            it[CARBS] = carbs
            it[FATS] = fats
            it[BMR] = bmr
            it[TEE] = tee
        }
    }

    val weight: Flow<String> = dataStore.data.map { it[WEIGHT] ?: "" }
    val height: Flow<String> = dataStore.data.map { it[HEIGHT] ?: "" }
    val age: Flow<String> = dataStore.data.map { it[AGE] ?: "" }
    val gender: Flow<String> = dataStore.data.map { it[GENDER] ?: "" }
    val activityLevel: Flow<String> = dataStore.data.map { it[ACTIVITY_LEVEL] ?: "" }
    val fitnessGoal: Flow<String> = dataStore.data.map { it[FITNESS_GOAL] ?: "" }
    val calorieGoal: Flow<String> = dataStore.data.map { it[CALORIE_GOAL] ?: "" }
    val protein: Flow<String> = dataStore.data.map { it[PROTEIN] ?: "" }
    val carbs: Flow<String> = dataStore.data.map { it[CARBS] ?: "" }
    val fats: Flow<String> = dataStore.data.map { it[FATS] ?: "" }
    val bmr: Flow<String> = dataStore.data.map { it[BMR] ?: "" }
    val tee: Flow<String> = dataStore.data.map { it[TEE] ?: "" }
}
