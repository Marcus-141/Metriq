package com.application.metriq.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.application.metriq.data.AppDatabase
import com.application.metriq.data.entity.LoggedFood
import com.application.metriq.data.entity.MealType
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DailyNutrients(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fats: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0,
    val cholesterol: Double = 0.0,
    val potassium: Double = 0.0,
    val saturatedFat: Double = 0.0,
    val vitaminD: Double = 0.0,
    val vitaminB12: Double = 0.0,
    val folate: Double = 0.0,
    val vitaminA: Double = 0.0,
    val vitaminC: Double = 0.0,
    val iron: Double = 0.0,
    val calcium: Double = 0.0,
    val magnesium: Double = 0.0,
    val iodine: Double = 0.0,
    val zinc: Double = 0.0
)

data class NutrientReport(
    val deficiencies: List<Deficiency>
)

data class Deficiency(
    val nutrientName: String,
    val currentAmount: Double,
    val goalAmount: Double,
    val topContributors: List<LoggedFood>
)

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

    val groupedConsumedFoods: StateFlow<Map<MealType, List<LoggedFood>>> = consumedFoods.map { foods ->
        foods.groupBy { MealType.valueOf(it.mealType) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val dailyTotals: StateFlow<DailyNutrients> = consumedFoods.map { foods ->
        calculateDailyNutrients(foods)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyNutrients())
    
    val historicalData: StateFlow<Map<LocalDate, DailyNutrients>> = dao.getAllFoods()
        .map { allFoods ->
            allFoods.groupBy {
                LocalDate.ofInstant(Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault())
            }
            .mapValues { entry -> calculateDailyNutrients(entry.value) }
            .toSortedMap(compareByDescending { it })
            .filterKeys { it.isAfter(LocalDate.now().minusDays(7)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // RDA Goals (Adult Average)
    private val nutrientGoals = mapOf(
        "Vitamin D" to 15.0, // mcg
        "Vitamin B12" to 2.4, // mcg
        "Folate" to 400.0, // mcg
        "Vitamin A" to 900.0, // mcg RAE
        "Vitamin C" to 90.0, // mg
        "Iron" to 18.0, // mg
        "Calcium" to 1000.0, // mg
        "Magnesium" to 400.0, // mg
        "Iodine" to 150.0, // mcg
        "Zinc" to 11.0 // mg
    )

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

    // Filter out unusable data
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

    fun addFood(summaryFood: Food, weight: Double, offset: Int, mealType: MealType) {
        viewModelScope.launch {
            var foodToUse = summaryFood
            try {
                // Fetch full details
                foodToUse = RetrofitInstance.api.getFoodDetails(summaryFood.fdcId)
            } catch (e: Exception) {
                Log.e("FoodNutritionViewModel", "Failed to fetch details for fdcId: ${summaryFood.fdcId}, falling back to summary", e)
                // foodToUse remains summaryFood
            }

            val caloriesKcal = (getNutrientValue(foodToUse.foodNutrients, "Energy", "KCAL") / 100) * weight
            val protein = (getNutrientValue(foodToUse.foodNutrients, "Protein") / 100) * weight
            val carbs = (getNutrientValue(foodToUse.foodNutrients, "Carbohydrate, by difference") / 100) * weight
            val fats = (getNutrientValue(foodToUse.foodNutrients, "Total lipid (fat)") / 100) * weight

            // Micronutrients (Previous)
            val fiber = (getNutrientValue(foodToUse.foodNutrients, "Fiber, total dietary") / 100) * weight
            val sugar = (getNutrientValue(foodToUse.foodNutrients, "Sugars, total including NLEA") / 100) * weight
            val sodium = (getNutrientValue(foodToUse.foodNutrients, "Sodium, Na") / 100) * weight
            val cholesterol = (getNutrientValue(foodToUse.foodNutrients, "Cholesterol") / 100) * weight
            val potassium = (getNutrientValue(foodToUse.foodNutrients, "Potassium, K") / 100) * weight
            val saturatedFat = (getNutrientValue(foodToUse.foodNutrients, "Fatty acids, total saturated") / 100) * weight
            
            // New Micronutrients (Advanced)
            val vitaminD = (getNutrientValue(foodToUse.foodNutrients, "Vitamin D (D2 + D3)") / 100) * weight
            val vitaminB12 = (getNutrientValue(foodToUse.foodNutrients, "Vitamin B-12") / 100) * weight
            val folate = (getNutrientValue(foodToUse.foodNutrients, "Folate, total") / 100) * weight
            val vitaminA = (getNutrientValue(foodToUse.foodNutrients, "Vitamin A, RAE") / 100) * weight
            val vitaminC = (getNutrientValue(foodToUse.foodNutrients, "Vitamin C, total ascorbic acid") / 100) * weight
            
            val iron = (getNutrientValue(foodToUse.foodNutrients, "Iron, Fe") / 100) * weight
            val calcium = (getNutrientValue(foodToUse.foodNutrients, "Calcium, Ca") / 100) * weight
            val magnesium = (getNutrientValue(foodToUse.foodNutrients, "Magnesium, Mg") / 100) * weight
            val iodine = (getNutrientValue(foodToUse.foodNutrients, "Iodine, I") / 100) * weight
            val zinc = (getNutrientValue(foodToUse.foodNutrients, "Zinc, Zn") / 100) * weight

            val foodName = if (!foodToUse.description.isNullOrBlank()) foodToUse.description else "Unknown Food"

            val timestamp = if (offset == 0) {
                System.currentTimeMillis()
            } else {
                try {
                    val zoneId = ZoneId.systemDefault()
                    val targetDate = LocalDate.now(zoneId).minusDays(offset.toLong())
                    // Set the time to 12:00 PM to ensure it falls within the day's start/end query range
                    targetDate.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
                } catch (e: Exception) {
                    Log.e("FoodNutritionViewModel", "Error calculating timestamp for offset $offset", e)
                    System.currentTimeMillis()
                }
            }

            val loggedFood = LoggedFood(
                name = foodName,
                calories = caloriesKcal,
                protein = protein,
                carbs = carbs,
                fats = fats,
                fiber = fiber,
                sugar = sugar,
                sodium = sodium,
                cholesterol = cholesterol,
                potassium = potassium,
                saturatedFat = saturatedFat,
                vitaminD = vitaminD,
                vitaminB12 = vitaminB12,
                folate = folate,
                vitaminA = vitaminA,
                vitaminC = vitaminC,
                iron = iron,
                calcium = calcium,
                magnesium = magnesium,
                iodine = iodine,
                zinc = zinc,
                weight = weight,
                timestamp = timestamp,
                mealType = mealType.name
            )
            dao.insert(loggedFood)
        }
    }

    fun deleteFood(food: LoggedFood) {
        viewModelScope.launch {
            dao.delete(food)
        }
    }

    private fun getNutrientValue(nutrients: List<FoodNutrient>, nutrientName: String, unitName: String? = null): Double {
        return nutrients.find { 
            it.nutrientName == nutrientName && (unitName == null || it.unitName == unitName)
        }?.value ?: 0.0
    }

    private fun calculateDailyNutrients(foods: List<LoggedFood>): DailyNutrients {
        return DailyNutrients(
            calories = foods.sumOf { it.calories },
            protein = foods.sumOf { it.protein },
            carbs = foods.sumOf { it.carbs },
            fats = foods.sumOf { it.fats },
            fiber = foods.sumOf { it.fiber },
            sugar = foods.sumOf { it.sugar },
            sodium = foods.sumOf { it.sodium },
            cholesterol = foods.sumOf { it.cholesterol },
            potassium = foods.sumOf { it.potassium },
            saturatedFat = foods.sumOf { it.saturatedFat },
            vitaminD = foods.sumOf { it.vitaminD },
            vitaminB12 = foods.sumOf { it.vitaminB12 },
            folate = foods.sumOf { it.folate },
            vitaminA = foods.sumOf { it.vitaminA },
            vitaminC = foods.sumOf { it.vitaminC },
            iron = foods.sumOf { it.iron },
            calcium = foods.sumOf { it.calcium },
            magnesium = foods.sumOf { it.magnesium },
            iodine = foods.sumOf { it.iodine },
            zinc = foods.sumOf { it.zinc }
        )
    }
    
    fun generateEndOfDayReport(loggedFoods: List<LoggedFood>): NutrientReport {
        // Calculate Totals
        val totals = mapOf(
            "Vitamin D" to loggedFoods.sumOf { it.vitaminD },
            "Vitamin B12" to loggedFoods.sumOf { it.vitaminB12 },
            "Folate" to loggedFoods.sumOf { it.folate },
            "Vitamin A" to loggedFoods.sumOf { it.vitaminA },
            "Vitamin C" to loggedFoods.sumOf { it.vitaminC },
            "Iron" to loggedFoods.sumOf { it.iron },
            "Calcium" to loggedFoods.sumOf { it.calcium },
            "Magnesium" to loggedFoods.sumOf { it.magnesium },
            "Iodine" to loggedFoods.sumOf { it.iodine },
            "Zinc" to loggedFoods.sumOf { it.zinc }
        )
        
        val deficiencies = mutableListOf<Deficiency>()
        
        totals.forEach { (nutrient, amount) ->
            val goal = nutrientGoals[nutrient] ?: 0.0
            if (goal > 0 && amount < (goal * 0.75)) {
                // Determine top 3 contributors
                val topContributors = loggedFoods
                    .sortedByDescending { food ->
                        when(nutrient) {
                            "Vitamin D" -> food.vitaminD
                            "Vitamin B12" -> food.vitaminB12
                            "Folate" -> food.folate
                            "Vitamin A" -> food.vitaminA
                            "Vitamin C" -> food.vitaminC
                            "Iron" -> food.iron
                            "Calcium" -> food.calcium
                            "Magnesium" -> food.magnesium
                            "Iodine" -> food.iodine
                            "Zinc" -> food.zinc
                            else -> 0.0
                        }
                    }
                    .take(3)
                
                deficiencies.add(Deficiency(nutrient, amount, goal, topContributors))
            }
        }
        
        // Return report with top 3 most deficient nutrients
        // Sort by percentage of goal met (lowest first)
        val sortedDeficiencies = deficiencies.sortedBy { it.currentAmount / it.goalAmount }.take(3)
        
        return NutrientReport(sortedDeficiencies)
    }
}
