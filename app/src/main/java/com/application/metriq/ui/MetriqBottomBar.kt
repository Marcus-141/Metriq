package com.application.metriq.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun MetriqBottomBar(navController: NavController, currentRoute: String?) {
    val activeColor = Color(0xFF00E676)
    val inactiveColor = Color.White

    BottomAppBar(containerColor = Color.Black) {
        val navItems = listOf(
            "dashboard" to Icons.Default.Home,
            "workout" to Icons.Default.FitnessCenter,
            "food" to Icons.Default.Fastfood,
            "profile" to Icons.Default.Person
        )

        navItems.forEach { (route, icon) ->
            IconButton(
                onClick = { navController.navigate(route) { launchSingleTop = true } },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = route.replaceFirstChar { it.uppercase() },
                    tint = if (currentRoute == route) activeColor else inactiveColor
                )
            }
        }
    }
}
