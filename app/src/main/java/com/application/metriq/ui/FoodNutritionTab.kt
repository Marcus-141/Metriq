package com.application.metriq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.metriq.network.Food
import com.application.metriq.ui.theme.MetriqTheme
import com.application.metriq.viewmodel.FoodNutritionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodNutritionTab(viewModel: FoodNutritionViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
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
fun FoodNutritionTabPreview() {
    MetriqTheme {
        FoodNutritionTab()
    }
}
