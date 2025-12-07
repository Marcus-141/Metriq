package com.application.metriq.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.* 
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.ui.theme.MetriqTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(navController: NavController) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Workout", "Food Nutrition")

    Scaffold(
        bottomBar = {
            BottomAppBar {
                IconButton(onClick = { navController.navigate("dashboard") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Home, contentDescription = "Dashboard")
                }
                IconButton(onClick = { navController.navigate("tracking") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Tracking")
                }
                IconButton(onClick = { navController.navigate("profile") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(text = title) })
                }
            }
            when (tabIndex) {
                0 -> WorkoutTab(navController)
                1 -> FoodNutritionTab(navController)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrackingScreenPreview() {
    MetriqTheme {
        TrackingScreen(navController = rememberNavController())
    }
}
