package com.application.metriq.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.ui.theme.MetriqTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Welcome to the Dashboard!",
                    style = TextStyle(fontSize = 24.sp, color = Color.Green)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MetriqTheme {
        DashboardScreen(navController = rememberNavController())
    }
}
