package com.application.metriq.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.data.entity.ExerciseSet
import com.application.metriq.data.entity.RoutineExercise
import com.application.metriq.data.entity.WorkoutRoutine
import com.application.metriq.ui.theme.MetriqTheme
import com.application.metriq.viewmodel.WorkoutViewModel

val WorkoutBlue = Color(0xFF2962FF)

@Composable
fun WorkoutScreen(navController: NavController, viewModel: WorkoutViewModel = viewModel()) {
    var isCreatingRoutine by remember { mutableStateOf(false) }

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
                        onClick = { /* TODO */ },
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
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = WorkoutBlue)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    onBack: () -> Unit,
    onSave: (String, List<RoutineExercise>) -> Unit
) {
    var routineName by remember { mutableStateOf("") }
    val exercises = listOf(
        "Bench Press", "Push Up", "Pull Up", "Barbell Row", "Squat", "Deadlift",
        "Overhead Press", "Bicep Curl", "Tricep Extension", "Plank", "Running"
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<String?>(null) }
    var addedExercises by remember { mutableStateOf<List<RoutineExercise>>(emptyList()) }

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
                    if (routineName.isNotBlank() && addedExercises.isNotEmpty()) {
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
                                addedExercises = addedExercises + RoutineExercise(name = it)
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
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(addedExercises) { exercise ->
                AddedExerciseCard(
                    exercise = exercise, 
                    onExerciseUpdate = { updatedExercise ->
                        addedExercises = addedExercises.map { if (it.name == exercise.name) updatedExercise else it }
                    }, 
                    onRemoveExercise = {
                        addedExercises = addedExercises.filter { it.name != exercise.name }
                    }
                )
            }
        }
    }
}

@Composable
fun AddedExerciseCard(
    exercise: RoutineExercise,
    onExerciseUpdate: (RoutineExercise) -> Unit,
    onRemoveExercise: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)), // Slightly more visible card
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = exercise.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onRemoveExercise) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Exercise", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Set", modifier = Modifier.weight(0.5f), color = Color.Gray, textAlign = TextAlign.Center)
                Text(text = "kg", modifier = Modifier.weight(1f), color = Color.Gray, textAlign = TextAlign.Center)
                Text(text = "Reps", modifier = Modifier.weight(1f), color = Color.Gray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(48.dp)) // For delete icon
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sets
            exercise.sets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}", 
                        modifier = Modifier.weight(0.5f),
                        color = Color.White, 
                        textAlign = TextAlign.Center
                    )
                    
                    CompactTextField(value = set.weight, onValueChange = {
                        val newSets = exercise.sets.toMutableList()
                        newSets[index] = newSets[index].copy(weight = it)
                        onExerciseUpdate(exercise.copy(sets = newSets))
                    }, modifier = Modifier.weight(1f).padding(horizontal = 4.dp))

                    CompactTextField(value = set.reps, onValueChange = {
                        val newSets = exercise.sets.toMutableList()
                        newSets[index] = newSets[index].copy(reps = it)
                        onExerciseUpdate(exercise.copy(sets = newSets))
                    }, modifier = Modifier.weight(1f).padding(horizontal = 4.dp))

                    IconButton(onClick = {
                        if (exercise.sets.size > 1) { 
                            val newSets = exercise.sets.toMutableList()
                            newSets.removeAt(index)
                            onExerciseUpdate(exercise.copy(sets = newSets))
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Set", tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val newSets = exercise.sets.toMutableList()
                    newSets.add(ExerciseSet())
                    onExerciseUpdate(exercise.copy(sets = newSets))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoutBlue.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "+ Add Set", color = WorkoutBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CompactTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(40.dp)
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(Color.White),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                innerTextField()
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun WorkoutScreenPreview() {
    MetriqTheme {
        WorkoutScreen(rememberNavController())
    }
}
