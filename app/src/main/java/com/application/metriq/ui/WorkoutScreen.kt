@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.application.metriq.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.data.entity.WorkoutRoutine
import com.application.metriq.ui.theme.MetriqTheme
import com.application.metriq.viewmodel.WorkoutViewModel

val WorkoutBlue = Color(0xFF2962FF)

@Composable
fun WorkoutScreen(navController: NavController, viewModel: WorkoutViewModel = viewModel()) {
    var isCreatingRoutine by remember { mutableStateOf(false) }
    
    // The Scaffold has been removed. The content is now managed by the root Scaffold in MainScreen.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isCreatingRoutine) {
            CreateRoutineScreen(
                onBack = { isCreatingRoutine = false },
                onSave = { name, exercises ->
                    viewModel.addRoutine(name, exercises)
                    isCreatingRoutine = false
                }
            )
        } else {
            WorkoutListScreen(
                viewModel = viewModel,
                onCreateRoutine = { isCreatingRoutine = true }
            )
        }
    }
}

@Composable
fun WorkoutListScreen(
    viewModel: WorkoutViewModel,
    onCreateRoutine: () -> Unit
) {
    val routines by viewModel.routines.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Workouts",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Start Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(WorkoutBlue, Color(0xFF448AFF))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "Quick Start",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start an empty workout and log as you go.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { /* Start empty workout */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Start Now",
                            color = WorkoutBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 20.dp),
                    tint = Color.White.copy(alpha = 0.1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Routines",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            TextButton(
                onClick = onCreateRoutine,
                colors = ButtonDefaults.textButtonColors(contentColor = WorkoutBlue),
                modifier = Modifier.background(WorkoutBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Routine", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No routines created yet.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(routines) { routine ->
                    RoutineItem(routine = routine)
                }
            }
        }
    }
}

@Composable
fun RoutineItem(routine: WorkoutRoutine) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = routine.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                val exerciseCount = routine.exercises.size
                Text(
                    text = if (exerciseCount == 1) "1 Exercise" else "$exerciseCount Exercises",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = { /* Start Routine */ }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = WorkoutBlue)
            }
        }
    }
}

@Composable
fun CreateRoutineScreen(
    onBack: () -> Unit,
    onSave: (String, List<String>) -> Unit
) {
    var routineName by remember { mutableStateOf("") }
    val exercises = listOf(
        "Bench Press (Chest)",
        "Push Up (Chest)",
        "Pull Up (Back)",
        "Barbell Row (Back)",
        "Squat (Legs)",
        "Deadlift (Legs)",
        "Overhead Press (Shoulders)",
        "Bicep Curl (Arms)",
        "Tricep Extension (Arms)",
        "Plank (Abs)",
        "Running (Cardio)"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<String?>(null) }
    var addedExercises by remember { mutableStateOf(listOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = "New Routine",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = { 
                    if (routineName.isNotBlank()) {
                        onSave(routineName, addedExercises)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28a745)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Routine Name",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = routineName,
            onValueChange = { routineName = it },
            placeholder = { Text("e.g. Leg Day, Push A", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Exercise",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedExercise ?: "Select an exercise...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Gray,
                                unfocusedBorderColor = Color.Gray,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color(0xFF2C2C2C))
                        ) {
                            exercises.forEach { exercise ->
                                DropdownMenuItem(
                                    text = { Text(text = exercise, color = Color.White) },
                                    onClick = {
                                        selectedExercise = exercise
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = { 
                            selectedExercise?.let {
                                addedExercises = addedExercises + it
                                selectedExercise = null
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = WorkoutBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add, 
                            contentDescription = "Add", 
                            tint = Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (addedExercises.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add exercises to build your routine.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
             LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(addedExercises) { exercise ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                         Text(
                             text = exercise,
                             modifier = Modifier.padding(16.dp),
                             color = Color.White
                         )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutScreenPreview() {
    MetriqTheme {
        WorkoutScreen(rememberNavController())
    }
}
