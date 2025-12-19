package com.application.metriq.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.metriq.data.entity.WorkoutLog
import com.application.metriq.viewmodel.WorkoutViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: WorkoutViewModel = viewModel()) {
    val history by viewModel.workoutHistory.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<WorkoutLog?>(null) }

    if (showDeleteDialog && logToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                logToDelete = null
            },
            title = { Text( text = "Delete Workout?") },
            text = { Text( text ="Are you sure you want to delete this log?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWorkoutLog(logToDelete!!)
                        showDeleteDialog = false
                        logToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        logToDelete = null
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Workout History",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No completed workouts yet.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(history, key = { it.id }) { log ->
                    HistoryItem(
                        log = log,
                        onLongClick = {
                            logToDelete = log
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryItem(
    log: WorkoutLog,
    onLongClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { expanded = !expanded },
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = log.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = Instant.ofEpochMilli(log.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Duration: ${formatDuration(log.duration)}",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                if (log.exercises.isEmpty()) {
                     Text(
                        text = "No exercises logged.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        log.exercises.forEach { exercise ->
                            Column {
                                Text(
                                    text = exercise.name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val setsText = if (exercise.sets.isEmpty()) {
                                    "No sets recorded"
                                } else {
                                    exercise.sets.joinToString(" | ") { set ->
                                        val weight = if (set.weight.isBlank()) "0" else set.weight
                                        val reps = if (set.reps.isBlank()) "0" else set.reps
                                        "${weight}kg x $reps"
                                    }
                                }
                                
                                Text(
                                    text = setsText,
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    return "${minutes}m ${seconds % 60}s"
}
