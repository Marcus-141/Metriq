package com.application.metriq.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.metriq.network.Food
import com.application.metriq.network.FoodNutrient
import com.application.metriq.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ConsumedFood(val food: Food, val weight: Double)

class FoodNutritionViewModel : ViewModel() {

    private val _searchResults = MutableStateFlow<List<Food>>(emptyList())
    val searchResults: StateFlow<List<Food>> = _searchResults

    private val _consumedFoods = MutableStateFlow<List<ConsumedFood>>(emptyList())
    val consumedFoods: StateFlow<List<ConsumedFood>> = _consumedFoods

    val totalProtein = _consumedFoods.map {
        it.sumOf { (food, weight) -> (getNutrientValue(food.foodNutrients, "Protein") / 100) * weight }
    }
    val totalCarbs = _consumedFoods.map {
        it.sumOf { (food, weight) -> (getNutrientValue(food.foodNutrients, "Carbohydrate, by difference") / 100) * weight }
    }
    val totalFats = _consumedFoods.map {
        it.sumOf { (food, weight) -> (getNutrientValue(food.foodNutrients, "Total lipid (fat)") / 100) * weight }
    }

    fun searchFoods(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                Log.d("FoodNutritionViewModel", "Searching for: $query")
                val response = RetrofitInstance.api.searchFoods(query)
                _searchResults.value = response.foods
                Log.d("FoodNutritionViewModel", "Found ${response.foods.size} results")
            } catch (e: Exception) {
                Log.e("FoodNutritionViewModel", "API call failed: ${e.message}", e)
            }
        }
    }

    fun addFood(food: Food, weight: Double) {
        _consumedFoods.value += ConsumedFood(food, weight)
    }

    private fun getNutrientValue(nutrients: List<FoodNutrient>, nutrientName: String): Double {
        return nutrients.find { it.nutrientName == nutrientName }?.value ?: 0.0
    }
}
