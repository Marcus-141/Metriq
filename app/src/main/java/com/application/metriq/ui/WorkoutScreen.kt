package com.application.metriq.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.application.metriq.ui.theme.MetriqTheme

@Composable
fun WorkoutScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Workout Tracking")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutScreenPreview() {
    MetriqTheme {
        WorkoutScreen()
    }
}
