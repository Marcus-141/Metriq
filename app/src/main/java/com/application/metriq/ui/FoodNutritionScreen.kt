package com.application.metriq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.network.Food
import com.application.metriq.ui.theme.MetriqTheme
import com.application.metriq.viewmodel.FoodNutritionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodNutritionScreen(navController: NavController, viewModel: FoodNutritionViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()

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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchFoods(it)
                    },
                    label = { Text("Search for a food") },
                    modifier = Modifier.fillMaxWidth(),
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
                        FoodListItem(food = food)
                    }
                }
            }
        }
    }
}

@Composable
fun FoodListItem(food: Food) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
