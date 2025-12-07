package com.application.metriq.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.application.metriq.network.Food
import com.application.metriq.ui.theme.MetriqTheme
import com.application.metriq.viewmodel.ConsumedFood
import com.application.metriq.viewmodel.FoodNutritionViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodNutritionScreen(viewModel: FoodNutritionViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<Food?>(null) }

    val totalProtein by viewModel.totalProtein.collectAsState(initial = 0.0)
    val totalCarbs by viewModel.totalCarbs.collectAsState(initial = 0.0)
    val totalFats by viewModel.totalFats.collectAsState(initial = 0.0)
    val consumedFoods by viewModel.consumedFoods.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            TodayNutritionCard(protein = totalProtein, carbs = totalCarbs, fats = totalFats)
            Spacer(modifier = Modifier.height(16.dp))
            LogFoodCard(searchQuery = searchQuery, onQueryChange = { searchQuery = it }, onSearch = { viewModel.searchFoods(searchQuery) })
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
            TodayMealsCard(consumedFoods = consumedFoods)
        }
    }
    if (showDialog) {
        AddFoodDialog(food = selectedFood!!, onDismiss = { showDialog = false }) { weight ->
            viewModel.addFood(selectedFood!!, weight)
            showDialog = false
        }
    }
}

@Composable
fun TodayNutritionCard(protein: Double, carbs: Double, fats: Double) {
    val df = DecimalFormat("#.##")
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PieChart, contentDescription = "Nutrition Chart", tint = Color.Green, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Today's Nutrition", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("0", style = MaterialTheme.typography.headlineSmall, color = Color.Green)
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

@OptIn(ExperimentalMaterial3Api::class)
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
fun TodayMealsCard(consumedFoods: List<ConsumedFood>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Today's Meals", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
            if (consumedFoods.isEmpty()) {
                Text("No meals logged today.", color = Color.Gray)
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
fun ConsumedFoodItem(consumedFood: ConsumedFood) {
    val df = DecimalFormat("#.##")
    val protein = (consumedFood.food.foodNutrients.find { it.nutrientName == "Protein" }?.value ?: 0.0) / 100 * consumedFood.weight
    val carbs = (consumedFood.food.foodNutrients.find { it.nutrientName == "Carbohydrate, by difference" }?.value ?: 0.0) / 100 * consumedFood.weight
    val fats = (consumedFood.food.foodNutrients.find { it.nutrientName == "Total lipid (fat)" }?.value ?: 0.0) / 100 * consumedFood.weight

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "${consumedFood.food.description} (${consumedFood.weight}g)")
        Text(text = "P: ${df.format(protein)}g, C: ${df.format(carbs)}g, F: ${df.format(fats)}g")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodDialog(food: Food, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var weight by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food.description) },
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = food.description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodNutritionScreenPreview() {
    MetriqTheme {
        FoodNutritionScreen()
    }
}
