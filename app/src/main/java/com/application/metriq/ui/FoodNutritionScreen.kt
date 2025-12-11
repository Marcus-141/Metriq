@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.application.metriq.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.application.metriq.data.entity.LoggedFood
import com.application.metriq.network.Food
import com.application.metriq.viewmodel.DailyNutrients
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
    
    val consumedFoods by viewModel.consumedFoods.collectAsState()
    val selectedDayOffset by viewModel.selectedDayOffset.collectAsState()
    val dailyTotals by viewModel.dailyTotals.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        DayNutritionCard(
            dailyNutrients = dailyTotals,
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
    
    val foodToLog = selectedFood
    if (showDialog && foodToLog != null) {
        AddFoodDialog(food = foodToLog, onDismiss = { 
            showDialog = false
            selectedFood = null 
        }) { weight ->
            viewModel.addFood(foodToLog, weight, selectedDayOffset)
            showDialog = false
            selectedFood = null
        }
    }
}

@Composable
fun DayNutritionCard(
    dailyNutrients: DailyNutrients,
    offset: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val df = DecimalFormat("#.##")
    var expanded by remember { mutableStateOf(false) }

    val title = remember(offset) {
        when (offset) {
            0 -> "Today's Nutrition"
            1 -> "Yesterday's Nutrition"
            else -> {
                val date = LocalDate.now().minusDays(offset.toLong())
                val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                "${date.format(formatter)} Nutrition"
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onPrev) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous Day",
                        tint = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    if (!expanded) {
                        Text(
                            text = "(Tap for details)",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onNext, enabled = offset > 0) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next Day",
                        tint = if (offset > 0) Color.Gray else Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Macros Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NutritionItem(
                    value = dailyNutrients.calories.toInt().toString(),
                    label = "CALORIES",
                    valueColor = Color(0xFF00E676),
                    labelColor = Color.Gray
                )

                NutritionItem(
                    value = df.format(dailyNutrients.protein),
                    label = "Protein (g)",
                    valueColor = Color.White,
                    labelColor = Color.Gray
                )

                NutritionItem(
                    value = df.format(dailyNutrients.carbs),
                    label = "Carbs (g)",
                    valueColor = Color.White,
                    labelColor = Color.Gray
                )

                NutritionItem(
                    value = df.format(dailyNutrients.fats),
                    label = "Fats (g)",
                    valueColor = Color.White,
                    labelColor = Color.Gray
                )
            }
            
            // Expandable Micronutrients Section
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Micronutrients",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Column
                        Column(modifier = Modifier.weight(1f)) {
                            MicroNutrientRow("Vitamin D", "${df.format(dailyNutrients.vitaminD)} µg")
                            MicroNutrientRow("Vitamin B12", "${df.format(dailyNutrients.vitaminB12)} µg")
                            MicroNutrientRow("Folate", "${df.format(dailyNutrients.folate)} µg")
                            MicroNutrientRow("Vitamin A", "${df.format(dailyNutrients.vitaminA)} µg")
                            MicroNutrientRow("Vitamin C", "${df.format(dailyNutrients.vitaminC)} mg")
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Right Column
                        Column(modifier = Modifier.weight(1f)) {
                            MicroNutrientRow("Iron", "${df.format(dailyNutrients.iron)} mg")
                            MicroNutrientRow("Calcium", "${df.format(dailyNutrients.calcium)} mg")
                            MicroNutrientRow("Magnesium", "${df.format(dailyNutrients.magnesium)} mg")
                            MicroNutrientRow("Iodine", "${df.format(dailyNutrients.iodine)} µg")
                            MicroNutrientRow("Zinc", "${df.format(dailyNutrients.zinc)} mg")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MicroNutrientRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NutritionItem(value: String, label: String, valueColor: Color, labelColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = labelColor,
            fontWeight = FontWeight.Medium
        )
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
    var isExpanded by remember { mutableStateOf(false) }

    val title = when (offset) {
        0 -> "Today's Consumption"
        1 -> "Yesterday's Consumption"
        else -> "Food Consumption"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded } // Toggle on card click
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color.White
                    )
                }
            }

            // Expandable Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn()
            ) {
                // Scrollable container with max height
                Column(
                    modifier = Modifier
                        .heightIn(max = 200.dp) // Limit height
                        .verticalScroll(rememberScrollState()) // Enable scrolling
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (consumedFoods.isEmpty()) {
                        Text(
                            text = "No meals logged.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        consumedFoods.forEach { consumedFood ->
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))
                            ConsumedFoodItem(consumedFood = consumedFood)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label:",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.alignByBaseline()
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alignByBaseline()
        )
    }
}

@Composable
fun ConsumedFoodItem(consumedFood: LoggedFood) {
    val df = DecimalFormat("#.##")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Top row: Food name and weight
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatFoodName(consumedFood.name),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Text(
                text = "${df.format(consumedFood.weight)}g",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom row: Macros, separated and color-coded
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MacroItem(
                label = "Kcal",
                value = consumedFood.calories.toInt().toString(),
                color = Color(0xFF00E676) // Bright Green
            )
            MacroItem(
                label = "P",
                value = "${df.format(consumedFood.protein)}g",
                color = Color(0xFF29B6F6) // Light Blue
            )
            MacroItem(
                label = "C",
                value = "${df.format(consumedFood.carbs)}g",
                color = Color(0xFF43A047) // Green
            )
            MacroItem(
                label = "F",
                value = "${df.format(consumedFood.fats)}g",
                color = Color(0xFFEF5350) // Red
            )
        }
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
