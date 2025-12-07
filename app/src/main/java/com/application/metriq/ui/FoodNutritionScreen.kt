@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.application.metriq.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.data.LoggedFood
import com.application.metriq.network.Food
import com.application.metriq.ui.theme.MetriqTheme
import com.application.metriq.viewmodel.FoodNutritionViewModel
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FoodNutritionScreen(navController: NavController, viewModel: FoodNutritionViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    
    // The consumedFoods list is now the single source of truth for the selected day.
    val consumedFoods by viewModel.consumedFoods.collectAsState()
    val selectedDayOffset by viewModel.selectedDayOffset.collectAsState()

    // CRASH FIX: Calculate totals directly and synchronously from the consumedFoods list.
    val totalProtein = consumedFoods.sumOf { it.protein }
    val totalCarbs = consumedFoods.sumOf { it.carbs }
    val totalFats = consumedFoods.sumOf { it.fats }

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = Color.Black) {
                IconButton(onClick = { navController.navigate("dashboard") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Home, contentDescription = "Dashboard", tint = Color.White)
                }
                IconButton(onClick = { navController.navigate("workout") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.FitnessCenter, contentDescription = "Workout", tint = Color.White)
                }
                IconButton(onClick = { navController.navigate("food") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Fastfood, contentDescription = "Food", tint = Color.White)
                }
                IconButton(onClick = { navController.navigate("profile") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                // Navigation Card for Nutrition
                DayNutritionCard(
                    protein = totalProtein, 
                    carbs = totalCarbs, 
                    fats = totalFats,
                    offset = selectedDayOffset,
                    onPrev = { 
                        if (selectedDayOffset < 30) {
                            viewModel.changeDay(1)
                        }
                    },
                    onNext = { 
                        if (selectedDayOffset > 0) {
                            viewModel.changeDay(-1)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                LogFoodCard(searchQuery = searchQuery, onQueryChange = { 
                    searchQuery = it
                    viewModel.searchFoods(it)
                }, onSearch = { 
                    Log.d("FoodNutritionScreen", "Search button clicked with query: $searchQuery")
                    viewModel.searchFoods(searchQuery) 
                })
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(searchResults) { food ->
                        FoodListItem(food = food, onClick = {
                            selectedFood = food
                            showDialog = true
                        })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TodayMealsCard(consumedFoods = consumedFoods, offset = selectedDayOffset)
            }
        }
    }
    
    // CRASH FIX: Ensure selectedFood is not null before showing dialog
    val foodToLog = selectedFood
    if (showDialog && foodToLog != null) {
        AddFoodDialog(food = foodToLog, onDismiss = { 
            showDialog = false
            selectedFood = null 
        }) { weight ->
            viewModel.addFood(foodToLog, weight)
            showDialog = false
            selectedFood = null
        }
    }
}

@Composable
fun DayNutritionCard(
    protein: Double, 
    carbs: Double, 
    fats: Double, 
    offset: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val df = DecimalFormat("#.##")
    
    val title = when (offset) {
        0 -> "Today's Nutrition"
        1 -> "Yesterday's Nutrition"
        else -> {
            try {
                val date = LocalDate.now().minusDays(offset.toLong())
                date.format(DateTimeFormatter.ofPattern("MMM d Nutrition", Locale.getDefault()))
            } catch (e: Exception) {
                "Nutrition History"
            }
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Arrows
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.SpaceBetween, 
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onPrev, enabled = offset < 30) { 
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = if (offset < 30) Color.White else Color.Gray) 
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PieChart, contentDescription = "Nutrition Chart", tint = Color.Green, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                IconButton(onClick = onNext, enabled = offset > 0) { 
                    Icon(
                        Icons.Default.ChevronRight, 
                        contentDescription = "Next Day", 
                        tint = if (offset > 0) Color.White else Color.Gray
                    ) 
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val approxCalories = (protein * 4) + (carbs * 4) + (fats * 9)
                    Text("${approxCalories.toInt()}", style = MaterialTheme.typography.headlineSmall, color = Color.Green)
                    Text("CALORIES", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${df.format(protein)}g", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text("Protein", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${df.format(carbs)}g", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text("Carbs", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${df.format(fats)}g", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text("Fats", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun LogFoodCard(searchQuery: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Log Food", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    label = { Text("Search food (e.g. Chicken, Egg)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White.copy(alpha = 0.1f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onSearch) {
                    Text("Search")
                }
            }
        }
    }
}

@Composable
fun TodayMealsCard(consumedFoods: List<LoggedFood>, offset: Int) {
    val title = when (offset) {
        0 -> "Today's Meals"
        1 -> "Yesterday's Meals"
        else -> "Meals"
    }
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
            if (consumedFoods.isEmpty()) {
                Text("No meals logged for this day.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(consumedFoods) { consumedFood ->
                        ConsumedFoodItem(consumedFood = consumedFood)
                    }
                }
            }
        }
    }
}

@Composable
fun ConsumedFoodItem(consumedFood: LoggedFood) {
    val df = DecimalFormat("#.##")
    val protein = consumedFood.protein
    val carbs = consumedFood.carbs
    val fats = consumedFood.fats

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "${formatFoodName(consumedFood.name)} (${consumedFood.weight}g)")
        Text(text = "P: ${df.format(protein)}g, C: ${df.format(carbs)}g, F: ${df.format(fats)}g")
    }
}

@Composable
fun AddFoodDialog(food: Food, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var weight by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(formatFoodName(food.description)) },
        text = {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (g)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                val weightValue = weight.toDoubleOrNull()
                if (weightValue != null) {
                    onConfirm(weightValue)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FoodListItem(food: Food, onClick: () -> Unit) {
    val calories = food.foodNutrients.find { it.nutrientName == "Energy" && it.unitName == "KCAL" }?.value ?: 0.0
    val protein = food.foodNutrients.find { it.nutrientName == "Protein" }?.value ?: 0.0
    val carbs = food.foodNutrients.find { it.nutrientName == "Carbohydrate, by difference" }?.value ?: 0.0
    val fats = food.foodNutrients.find { it.nutrientName == "Total lipid (fat)" }?.value ?: 0.0
    val df = DecimalFormat("#.##")

    val foodName = when {
        !food.description.isNullOrBlank() -> food.description
        !food.additionalDescriptions.isNullOrBlank() -> food.additionalDescriptions
        else -> "Unknown Food"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${formatFoodName(foodName)} (100g)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${calories.toInt()} kcal • P: ${df.format(protein)}g C: ${df.format(carbs)}g F: ${df.format(fats)}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFFE8F5E9),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun formatFoodName(name: String?): String {
    if (name.isNullOrBlank()) return "Unknown Food"
    val cleanedName = name
        .replace("(?i)ns as to".toRegex(), "")
        .replace(";", ",")
    
    val smallWords = setOf("and", "with", "or", "in", "of", "a", "an", "the", "to", "for", "at", "by", "on")

    return cleanedName.lowercase().split("\\s+".toRegex()).mapIndexed { index, word ->
        if (word.isEmpty()) return@mapIndexed ""
        
        val bareWord = word.filter { it.isLetter() }
        if (index > 0 && smallWords.contains(bareWord)) {
            word // keep lowercase
        } else {
            // Capitalize first letter
            val firstLetterIdx = word.indexOfFirst { it.isLetter() }
            if (firstLetterIdx != -1) {
                word.substring(0, firstLetterIdx) + word[firstLetterIdx].uppercase() + word.substring(firstLetterIdx + 1)
            } else {
                word
            }
        }
    }.joinToString(" ")
}
