package com.application.metriq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.metriq.data.entity.RoutineExercise
import com.application.metriq.data.entity.WorkoutRoutine
import kotlinx.coroutines.delay
import com.application.metriq.ui.theme.WorkoutBlue


@Composable
fun ActiveWorkoutScreen(routine: WorkoutRoutine?, onFinish: (Long, List<RoutineExercise>) -> Unit) {
    var elapsedTime by remember { mutableStateOf(0L) }
    val exercises = remember { mutableStateListOf<RoutineExercise>().apply { addAll(routine?.exercises ?: emptyList()) } }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedTime++
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = routine?.name ?: "Quick Start Workout",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = formatDuration(elapsedTime),
                fontSize = 20.sp,
                color = WorkoutBlue
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(exercises) { exercise ->
                // You can reuse or adapt your AddedExerciseCard here
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onFinish(elapsedTime, exercises) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28a745))
        ) {
            Text("Finish Workout")
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}
