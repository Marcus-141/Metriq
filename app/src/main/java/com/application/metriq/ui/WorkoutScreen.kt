@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.application.metriq.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
fun WorkoutScreen(navController: NavController, viewModel: WorkoutViewModel = viewModel()) {
    var isCreatingRoutine by remember { mutableStateOf(false) }
    
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
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color(0xFFF5F7FA)
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
                            colors = listOf(Color(0xFF2962FF), Color(0xFF448AFF))
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
                            color = Color(0xFF2962FF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Decorative triangle/shape
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 20.dp)
                        .padding(end = 0.dp),
                    tint = Color.White.copy(alpha = 0.1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // My Routines Header
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
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2962FF)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Routine", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Routines List
        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)), // Dashed border simulated
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    color = Color.Black
                )
                val exerciseCount = routine.exercises.size
                Text(
                    text = if (exerciseCount == 1) "1 Exercise" else "$exerciseCount Exercises",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = { /* Start Routine */ }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color(0xFF2962FF))
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
    // List of exercises
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color(0xFFE3F2FD), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1565C0))
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
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Routine Name Input
        Text(
            text = "Routine Name",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray, // Dark Blue/Gray
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = routineName,
            onValueChange = { routineName = it },
            placeholder = { Text("e.g. Leg Day, Push A", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF333333), // Dark background input like image
                unfocusedContainerColor = Color(0xFF333333),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Add Exercise Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Exercise",
                    fontSize = 12.sp,
                    color = Color.Gray, // Dark Blue/Gray
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    // Dropdown
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
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            exercises.forEach { exercise ->
                                DropdownMenuItem(
                                    text = { Text(text = exercise, color = Color.Black) },
                                    onClick = {
                                        selectedExercise = exercise
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    // Plus Button
                    FilledIconButton(
                        onClick = { 
                            selectedExercise?.let {
                                addedExercises = addedExercises + it
                                selectedExercise = null
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE3F2FD)), // Light blue
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(56.dp) // Standard height for TextField
                    ) {
                        Icon(
                            Icons.Filled.Add, 
                            contentDescription = "Add", 
                            tint = Color(0xFF1976D2) // Darker blue
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Empty State or List of Added Exercises
        if (addedExercises.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Color.LightGray,
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                         Text(
                             text = exercise,
                             modifier = Modifier.padding(16.dp),
                             color = Color.Black
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
