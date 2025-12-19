package com.application.metriq.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview


//simple data class to represent each navigation item
private data class BottomNavItem(val route: String, val icon: ImageVector, val contentDescription: String)

@Composable
fun MetriqBottomBar(navController: NavController, currentRoute: String?) {
    val navItems = listOf(
        BottomNavItem("dashboard", Icons.Default.Home, "Dashboard"),
        BottomNavItem("workout", Icons.Default.FitnessCenter, "Workout"),
        BottomNavItem("food", Icons.Default.Fastfood, "Food"),
        BottomNavItem("history", Icons.Default.History, "History"),
        BottomNavItem("profile", Icons.Default.Person, "Profile")
    )

    BottomAppBar(containerColor = Color.Black) {
        navItems.forEach { item ->
            IconButton(
                onClick = {
                    // Prevent navigating to the same screen, which causes the flicker
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large back stack.
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.contentDescription,
                    tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else Color.White
                )
            }
        }
    }
}
