package com.application.metriq.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.network.Food
import com.application.metriq.ui.theme.MetriqTheme
import com.application.metriq.viewmodel.FoodNutritionViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodNutritionScreen(navController: NavController, viewModel: FoodNutritionViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<Food?>(null) }

    val totalProtein by viewModel.totalProtein.collectAsState(initial = 0.0)
    val totalCarbs by viewModel.totalCarbs.collectAsState(initial = 0.0)
    val totalFats by viewModel.totalFats.collectAsState(initial = 0.0)

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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TotalNutrients(protein = totalProtein, carbs = totalCarbs, fats = totalFats)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchFoods(it)
                    },
                    label = { Text("Search for a food") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White.copy(alpha = 0.1f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { food ->
                        FoodListItem(food = food, onClick = {
                            selectedFood = food
                            showDialog = true
                        })
                    }
                }
            }
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
fun TotalNutrients(protein: Double, carbs: Double, fats: Double) {
    val df = DecimalFormat("#.##")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Protein")
            Text(df.format(protein), style = MaterialTheme.typography.headlineSmall)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Carbs")
            Text(df.format(carbs), style = MaterialTheme.typography.headlineSmall)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Fats")
            Text(df.format(fats), style = MaterialTheme.typography.headlineSmall)
        }
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
            food.foodNutrients.forEach { nutrient ->
                Text(text = "${nutrient.nutrientName}: ${nutrient.value} ${nutrient.unitName}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodNutritionScreenPreview() {
    MetriqTheme {
        FoodNutritionScreen(navController = rememberNavController())
    }
}
