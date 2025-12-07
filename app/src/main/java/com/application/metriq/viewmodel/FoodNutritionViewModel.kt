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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FoodNutritionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).loggedFoodDao()

    private val _searchResults = MutableStateFlow<List<Food>>(emptyList())
    val searchResults: StateFlow<List<Food>> = _searchResults

    private var searchJob: Job? = null

    // Get start and end of today
    private val todayRange: Pair<Long, Long>
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val end = calendar.timeInMillis
            return start to end
        }

    val consumedFoods: StateFlow<List<LoggedFood>> = dao.getFoodsForDate(todayRange.first, todayRange.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalProtein = consumedFoods.map { list -> list.sumOf { it.protein } }
    val totalCarbs = consumedFoods.map { list -> list.sumOf { it.carbs } }
    val totalFats = consumedFoods.map { list -> list.sumOf { it.fats } }

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

    fun addFood(food: Food, weight: Double) {
        viewModelScope.launch {
            val calories = (getNutrientValue(food.foodNutrients, "Energy") / 100) * weight // Assuming Energy is KCAL
            // Note: USDA sometimes has "Energy" with unit "kJ" or "kcal". Usually "Energy" with id 1008 is kcal.
            // Better check for KCAL unit if possible, or assume Energy is kcal.
            // In FoodListItem we check unitName == "KCAL". Let's do same here.
            val caloriesKcal = (food.foodNutrients.find { it.nutrientName == "Energy" && it.unitName == "KCAL" }?.value ?: 0.0) / 100 * weight
            
            val protein = (getNutrientValue(food.foodNutrients, "Protein") / 100) * weight
            val carbs = (getNutrientValue(food.foodNutrients, "Carbohydrate, by difference") / 100) * weight
            val fats = (getNutrientValue(food.foodNutrients, "Total lipid (fat)") / 100) * weight

            val loggedFood = LoggedFood(
                name = food.description,
                calories = caloriesKcal,
                protein = protein,
                carbs = carbs,
                fats = fats,
                weight = weight,
                timestamp = System.currentTimeMillis()
            )
            dao.insert(loggedFood)
        }
    }

    private fun getNutrientValue(nutrients: List<FoodNutrient>, nutrientName: String): Double {
        return nutrients.find { it.nutrientName == nutrientName }?.value ?: 0.0
    }
}
