package com.application.metriq.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.ui.theme.MetriqTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(navController: NavController) {
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Workout Tracking")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutScreenPreview() {
    MetriqTheme {
        WorkoutScreen(navController = rememberNavController())
    }
}
