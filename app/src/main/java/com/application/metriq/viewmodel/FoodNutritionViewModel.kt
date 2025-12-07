package com.application.metriq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.metriq.network.Food
import com.application.metriq.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FoodNutritionViewModel : ViewModel() {

    private val _searchResults = MutableStateFlow<List<Food>>(emptyList())
    val searchResults: StateFlow<List<Food>> = _searchResults

    fun searchFoods(query: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchFoods(query)
                _searchResults.value = response.foods
            } catch (_: Exception) {
                // Handle error
            }
        }
    }
}
