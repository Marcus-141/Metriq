@file:OptIn(ExperimentalMaterial3Api::class)

package com.application.metriq.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.application.metriq.data.entity.LoggedFood
import com.application.metriq.data.entity.MealType
import com.application.metriq.network.Food
import com.application.metriq.ui.theme.*
import com.application.metriq.viewmodel.DailyNutrients
import com.application.metriq.viewmodel.FoodNutritionViewModel
import com.application.metriq.viewmodel.ProfileViewModel
import com.application.metriq.viewmodel.UserGoals
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FoodNutritionScreen(
    navController: NavController, 
    viewModel: FoodNutritionViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    
    val groupedConsumedFoods by viewModel.groupedConsumedFoods.collectAsState()
    val selectedDayOffset by viewModel.selectedDayOffset.collectAsState()
    val dailyTotals by viewModel.dailyTotals.collectAsState()
    val userGoals by profileViewModel.userGoals.collectAsState()
    
    var selectedMealType by remember { mutableStateOf(MealType.BREAKFAST) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        DayNutritionCard(
            dailyNutrients = dailyTotals,
            userGoals = userGoals,
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
        
        LogFoodCard(
            searchQuery = searchQuery, 
            selectedMealType = selectedMealType,
            onMealTypeChange = { selectedMealType = it },
            onQueryChange = { 
                searchQuery = it
                viewModel.searchFoods(it)
            }, 
            onSearch = { 
                Log.d("FoodNutritionScreen", "Search button clicked with query: $searchQuery")
                viewModel.searchFoods(searchQuery) 
            }
        )
        
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
        TodayMealsCard(
            groupedFoods = groupedConsumedFoods, 
            offset = selectedDayOffset,
            onDeleteFood = { food -> viewModel.deleteFood(food) }
        )
    }
    
    val foodToLog = selectedFood
    if (showDialog && foodToLog != null) {
        AddFoodDialog(food = foodToLog, onDismiss = { 
            showDialog = false
            selectedFood = null 
        }) { weight ->
            viewModel.addFood(foodToLog, weight, selectedDayOffset, selectedMealType)
            showDialog = false
            selectedFood = null
        }
    }
}

@Composable
fun DayNutritionCard(
    dailyNutrients: DailyNutrients,
    userGoals: UserGoals,
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
        colors = CardDefaults.cardColors(containerColor = CardBackground),
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
                        tint = TextGray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = null,
                            tint = LogoCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    if (!expanded) {
                        Text(
                            text = "(Tap for details)",
                            fontSize = 10.sp,
                            color = TextGray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onNext, enabled = offset > 0) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next Day",
                        tint = if (offset > 0) TextGray else Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Macros Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NutritionProgressItem(
                    current = dailyNutrients.calories,
                    goal = userGoals.calorieGoal,
                    label = "CALORIES",
                    valueColor = PrimaryGreen,
                    labelColor = TextGray,
                    isCalorie = true
                )

                NutritionProgressItem(
                    current = dailyNutrients.protein,
                    goal = userGoals.proteinGoal,
                    label = "Protein (g)",
                    valueColor = NutrientProtein,
                    labelColor = TextGray
                )

                NutritionProgressItem(
                    current = dailyNutrients.carbs,
                    goal = userGoals.carbsGoal,
                    label = "Carbs (g)",
                    valueColor = NutrientCarbs,
                    labelColor = TextGray
                )

                NutritionProgressItem(
                    current = dailyNutrients.fats,
                    goal = userGoals.fatsGoal,
                    label = "Fats (g)",
                    valueColor = NutrientFats,
                    labelColor = TextGray
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
                    HorizontalDivider(color = TextGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Micronutrients",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
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
        Text(text = label, color = TextGray, fontSize = 14.sp)
        Text(text = value, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NutritionProgressItem(
    current: Double, 
    goal: Double, 
    label: String, 
    valueColor: Color, 
    labelColor: Color,
    isCalorie: Boolean = false
) {
    val progress = (current / goal).coerceIn(0.0, 1.0).toFloat()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            text = "${current.toInt()} / ${goal.toInt()}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .padding(horizontal = 4.dp),
            color = valueColor,
            trackColor = valueColor.copy(alpha = 0.2f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = labelColor,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MealTypeSelector(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MealType.values().forEach { mealType ->
            FilterChip(
                selected = mealType == selectedMealType,
                onClick = { onMealTypeSelected(mealType) },
                label = { 
                    Text(
                        text = mealType.displayName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LogoCyan,
                    selectedLabelColor = Color.Black,
                    containerColor = CardBackgroundFaded,
                    labelColor = TextWhite
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true, 
                    selected = mealType == selectedMealType, 
                    borderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun LogFoodCard(
    searchQuery: String, 
    selectedMealType: MealType,
    onMealTypeChange: (MealType) -> Unit,
    onQueryChange: (String) -> Unit, 
    onSearch: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundFaded),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Log Food", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.padding(bottom = 8.dp))
            
            MealTypeSelector(
                selectedMealType = selectedMealType,
                onMealTypeSelected = onMealTypeChange
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
                        unfocusedContainerColor = CardBackgroundFaded,
                        focusedContainerColor = CardBackgroundFaded
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodayMealsCard(
    groupedFoods: Map<MealType, List<LoggedFood>>, 
    offset: Int,
    onDeleteFood: (LoggedFood) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) } // Expanded by default
    val pagerState = rememberPagerState { MealType.values().size }
    val coroutineScope = rememberCoroutineScope()

    val title = when (offset) {
        0 -> "Today's Consumption"
        1 -> "Yesterday's Consumption"
        else -> "Food Consumption"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundFaded),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextWhite
                    )
                }
            }

            // Expandable Content with Tabs and Pager
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        contentColor = PrimaryGreen,
                        edgePadding = 0.dp,
                        divider = { HorizontalDivider(color = TextGray.copy(alpha = 0.3f)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MealType.values().forEachIndexed { index, mealType ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                                text = { 
                                    Text(
                                        text = mealType.displayName,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    ) 
                                }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.height(250.dp) // Give the pager a fixed height
                    ) {
                        val mealType = MealType.values()[it]
                        val foodsForMeal = groupedFoods[mealType] ?: emptyList()

                        if (foodsForMeal.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No ${mealType.displayName} logged",
                                    color = TextGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(foodsForMeal) { food ->
                                    ConsumedFoodItem(consumedFood = food, onDelete = onDeleteFood)
                                }
                            }
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
            color = TextGray,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConsumedFoodItem(consumedFood: LoggedFood, onDelete: (LoggedFood) -> Unit) {
    val df = DecimalFormat("#.##")
    val haptic = LocalHapticFeedback.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Food") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(consumedFood)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { }, // Handle regular click if needed
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDeleteDialog = true
                }
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top row: Food name and weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = formatFoodName(consumedFood.name),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (consumedFood.mealType.isNotEmpty()) {
                        Text(
                            text = consumedFood.mealType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = "${df.format(consumedFood.weight)}g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
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
                    color = PrimaryGreen // Bright Green
                )
                MacroItem(
                    label = "P",
                    value = "${df.format(consumedFood.protein)}g",
                    color = NutrientProtein // Light Blue
                )
                MacroItem(
                    label = "C",
                    value = "${df.format(consumedFood.carbs)}g",
                    color = NutrientCarbs // Green
                )
                MacroItem(
                    label = "F",
                    value = "${df.format(consumedFood.fats)}g",
                    color = NutrientFats // Red
                )
            }
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
        colors = CardDefaults.cardColors(containerColor = CardBackgroundFaded)
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
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${calories.toInt()} kcal • P: ${df.format(protein)}g C: ${df.format(carbs)}g F: ${df.format(fats)}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
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
