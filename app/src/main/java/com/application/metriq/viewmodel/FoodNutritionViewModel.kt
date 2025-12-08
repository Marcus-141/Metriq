package com.application.metriq.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.application.metriq.data.AppDatabase
import com.application.metriq.data.LoggedFood
import com.application.metriq.network.Food
import com.application.metriq.network.FoodNutrient
import com.application.metriq.network.RetrofitInstance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class FoodNutritionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).loggedFoodDao()

    private val _searchResults = MutableStateFlow<List<Food>>(emptyList())
    val searchResults: StateFlow<List<Food>> = _searchResults

    private var searchJob: Job? = null

    private val _selectedDayOffset = MutableStateFlow(0) // 0 = Today, 1 = Yesterday, etc.
    val selectedDayOffset = _selectedDayOffset.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val consumedFoods: StateFlow<List<LoggedFood>> = _selectedDayOffset.flatMapLatest { offset ->
        val (start, end) = getStartEndOfDay(offset)
        dao.getFoodsForDate(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun changeDay(amount: Int) {
        val currentOffset = _selectedDayOffset.value
        val newOffset = currentOffset + amount
        // Ensure offset is strictly between 0 (Today) and 30 (30 days ago) to prevent navigation errors
        _selectedDayOffset.value = newOffset.coerceIn(0, 30)
    }

    private fun getStartEndOfDay(offset: Int): Pair<Long, Long> {
        return try {
            val zoneId = ZoneId.systemDefault()
            val today = LocalDate.now(zoneId)
            val targetDate = today.minusDays(offset.toLong())
            
            val start = targetDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = targetDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            
            start to end
        } catch (e: Exception) {
            Log.e("FoodNutritionViewModel", "Error calculating date range for offset $offset", e)
            // Fallback to today's range if calculation fails
            val now = System.currentTimeMillis()
            val start = now - (now % 86400000) // Rough approximation of start of day
            val end = start + 86400000
            start to end
        }
    }

    fun searchFoods(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // Debounce for 500ms
            try {
                Log.d("FoodNutritionViewModel", "Searching for: $query")
                val dataTypes = listOf("Foundation", "SR Legacy", "Survey (FNDDS)")
                val response = RetrofitInstance.api.searchFoods(query, dataTypes)
                _searchResults.value = response.foods
                Log.d("FoodNutritionViewModel", "Found ${response.foods.size} results")
            } catch (e: Exception) {
                Log.e("FoodNutritionViewModel", "API call failed: ${e.message}", e)
            }
        }
    }

    fun addFood(food: Food, weight: Double, offset: Int) {
        viewModelScope.launch {
            val caloriesKcal = (food.foodNutrients.find { it.nutrientName == "Energy" && it.unitName == "KCAL" }?.value ?: 0.0) / 100 * weight
            val protein = (getNutrientValue(food.foodNutrients, "Protein") / 100) * weight
            val carbs = (getNutrientValue(food.foodNutrients, "Carbohydrate, by difference") / 100) * weight
            val fats = (getNutrientValue(food.foodNutrients, "Total lipid (fat)") / 100) * weight

            val foodName = if (!food.description.isNullOrBlank()) food.description else "Unknown Food"

            val timestamp = if (offset == 0) {
                System.currentTimeMillis()
            } else {
                val (startOfDay, _) = getStartEndOfDay(offset)
                // Use noon (12:00 PM) of the past date to be safe
                startOfDay + 43200000L 
            }

            val loggedFood = LoggedFood(
                name = foodName,
                calories = caloriesKcal,
                protein = protein,
                carbs = carbs,
                fats = fats,
                weight = weight,
                timestamp = timestamp
            )
            dao.insert(loggedFood)
        }
    }

    private fun getNutrientValue(nutrients: List<FoodNutrient>, nutrientName: String): Double {
        return nutrients.find { it.nutrientName == nutrientName }?.value ?: 0.0
    }
}
