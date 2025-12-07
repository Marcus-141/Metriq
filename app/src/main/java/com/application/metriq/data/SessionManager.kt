package com.application.metriq.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val WEIGHT = stringPreferencesKey("weight")
        val HEIGHT = stringPreferencesKey("height")
        val AGE = stringPreferencesKey("age")
        val GENDER = stringPreferencesKey("gender")
        val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        val PROTEIN = stringPreferencesKey("protein")
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
        protein: String,
        bmr: String,
        tee: String
    ) {
        dataStore.edit {
            it[WEIGHT] = weight
            it[HEIGHT] = height
            it[AGE] = age
            it[GENDER] = gender
            it[ACTIVITY_LEVEL] = activityLevel
            it[PROTEIN] = protein
            it[BMR] = bmr
            it[TEE] = tee
        }
    }

    val weight: Flow<String> = dataStore.data.map { it[WEIGHT] ?: "" }
    val height: Flow<String> = dataStore.data.map { it[HEIGHT] ?: "" }
    val age: Flow<String> = dataStore.data.map { it[AGE] ?: "" }
    val gender: Flow<String> = dataStore.data.map { it[GENDER] ?: "" }
    val activityLevel: Flow<String> = dataStore.data.map { it[ACTIVITY_LEVEL] ?: "" }
    val protein: Flow<String> = dataStore.data.map { it[PROTEIN] ?: "" }
    val bmr: Flow<String> = dataStore.data.map { it[BMR] ?: "" }
    val tee: Flow<String> = dataStore.data.map { it[TEE] ?: "" }
}
