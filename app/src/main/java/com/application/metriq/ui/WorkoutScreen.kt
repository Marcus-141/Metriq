package com.application.metriq.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.data.ExerciseSet
import com.application.metriq.data.RoutineExercise
import com.application.metriq.data.WorkoutRoutine
import com.application.metriq.ui.theme.LogoCyan
import com.application.metriq.ui.theme.MetriqTheme
import com.application.metriq.ui.theme.WorkoutBlue
import com.application.metriq.viewmodel.WorkoutViewModel

@Composable
fun WorkoutScreen(navController: NavController, viewModel: WorkoutViewModel = viewModel()) {
    var isEditingRoutine by remember { mutableStateOf(false) }
    var viewingRoutine by remember { mutableStateOf<WorkoutRoutine?>(null) }
    var selectedRoutineForEdit by remember { mutableStateOf<WorkoutRoutine?>(null) }

    var isWorkoutActive by remember{mutableStateOf(false)}
    var activeRoutine by remember{mutableStateOf<WorkoutRoutine?>(null)}

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            isWorkoutActive ->{
                ActiveWorkoutScreen(
                    routine = activeRoutine,
                    onFinish = {
                        log->
                        viewModel.saveWorkout(log)
                        isWorkoutActive = false
                        activeRoutine = null
                        viewingRoutine = null
                    },
                    onCancel = {
                        isWorkoutActive = false
                        activeRoutine = null
                    }
                )
            }
            isEditingRoutine -> {
                CreateRoutineScreen(
                    routine = selectedRoutineForEdit,
                    viewModel = viewModel,
                    onBack = {
                        isEditingRoutine = false
                        selectedRoutineForEdit = null
                    },
                    onSave = { routine ->
                        if (routine.id == 0L) {
                            viewModel.addRoutine(routine.name, routine.exercises)
                        } else {
                            viewModel.updateRoutine(routine)
                        }
                        isEditingRoutine = false
                        selectedRoutineForEdit = null
                    }
                )
            }
            viewingRoutine != null -> {
                ViewRoutineScreen(
                    routine = viewingRoutine!!,
                    onBack = { viewingRoutine = null },
                    onEdit = {
                        selectedRoutineForEdit = viewingRoutine
                        viewingRoutine = null
                        isEditingRoutine = true
                    }
                )
            }
            else -> {
                WorkoutListScreen(
                    viewModel = viewModel,
                    onCreateRoutine = { 
                        selectedRoutineForEdit = null
                        isEditingRoutine = true 
                    },
                    onViewRoutine = { routine ->
                        viewingRoutine = routine
                    },
                    onEditRoutine = { routine ->
                        selectedRoutineForEdit = routine
                        isEditingRoutine = true
                    },
                    onDeleteRoutine = { routine ->
                        viewModel.deleteRoutine(routine)
                    },
                    onQuickStart = {
                        activeRoutine = null
                        isWorkoutActive = true
                    }
                )
            }
        }
    }
}

@Composable
fun WorkoutListScreen(
    viewModel: WorkoutViewModel,
    onCreateRoutine: () -> Unit,
    onViewRoutine: (WorkoutRoutine) -> Unit,
    onEditRoutine: (WorkoutRoutine) -> Unit,
    onDeleteRoutine: (WorkoutRoutine) -> Unit,
    onQuickStart: () -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    val history by viewModel.workoutHistory.collectAsState()
    val workoutCount = history.size
    val totalMinutes = history.sumOf { it.duration } / 60

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatsCard(title = "WORKOUTS", value = workoutCount.toString(), modifier = Modifier.weight(1f))
            StatsCard(title = "MINUTES", value = totalMinutes.toString(), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable(onClick = onQuickStart),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LogoCyan)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Empty Workout",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Start logging from scratch",
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

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
            
            Button(
                onClick = onCreateRoutine,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6FFFF)),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Routine", color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                    RoutineItem(
                        routine = routine, 
                        onView = { onViewRoutine(routine) },
                        onEdit = { onEditRoutine(routine) },
                        onDelete = { onDeleteRoutine(routine) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RoutineItem(
    routine: WorkoutRoutine,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onView), // Main card click triggers view
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val exerciseCount = routine.exercises.size
                Text(
                    text = if (exerciseCount == 1) "1 Exercise" else "$exerciseCount Exercises",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            expanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ViewRoutineScreen(
    routine: WorkoutRoutine,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
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
                text = "Routine Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
             Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF448AFF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = routine.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(routine.exercises) { exercise ->
                 Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                         Text(
                             text = exercise.name, 
                             color = Color.White, 
                             fontWeight = FontWeight.Bold, 
                             fontSize = 18.sp,
                             modifier = Modifier.padding(bottom = 8.dp),
                             maxLines = 1,
                             overflow = TextOverflow.Ellipsis
                         )

                        // Headers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "Set", modifier = Modifier.weight(0.5f), color = Color.Gray, textAlign = TextAlign.Center)
                            Text(text = "kg", modifier = Modifier.weight(1f), color = Color.Gray, textAlign = TextAlign.Center)
                            Text(text = "Reps", modifier = Modifier.weight(1f), color = Color.Gray, textAlign = TextAlign.Center)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Sets
                        exercise.sets.forEachIndexed { index, set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}", 
                                    modifier = Modifier.weight(0.5f),
                                    color = Color.White, 
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = set.weight, 
                                    modifier = Modifier.weight(1f),
                                    color = Color.White, 
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = set.reps, 
                                    modifier = Modifier.weight(1f),
                                    color = Color.White, 
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    routine: WorkoutRoutine?,
    viewModel: WorkoutViewModel = viewModel(),
    onBack: () -> Unit,
    onSave: (WorkoutRoutine) -> Unit
) {
    var routineName by remember { mutableStateOf(routine?.name ?: "") }
    
    //Updated logic for exercise search
    val context = LocalContext.current
    
    //Collect exercises from ViewModel
    val allExercises by viewModel.allExercises.collectAsState()
    val exercises = remember(allExercises) { allExercises.map { it.name } }
    
    //Search state
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    //Filter logic - Optimized with derivedStateOf
    val filteredExercises by remember {
        derivedStateOf {
            if (searchText.isBlank()) emptyList()
            else exercises.filter { it.contains(searchText, ignoreCase = true) }
        }
    }

    val addedExercises = remember { mutableStateListOf<RoutineExercise>().apply { addAll(routine?.exercises ?: emptyList()) } }

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
                text = if (routine == null) "New Routine" else "Edit Routine",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { 
                    if (routineName.isBlank()) {
                        Toast.makeText(context, "Please enter a routine name.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (addedExercises.isEmpty()) {
                        Toast.makeText(context, "Please add at least one exercise.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val newRoutine = routine?.copy(name = routineName, exercises = addedExercises) ?: WorkoutRoutine(name = routineName, exercises = addedExercises)
                    onSave(newRoutine)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28a745)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", maxLines = 1, overflow = TextOverflow.Ellipsis)
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

        //REPLACED CARD FOR SEARCH BAR
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

                // Search Bar Container
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { 
                            searchText = it
                            expanded = true
                        },
                        placeholder = { Text("Search exercise...", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                textFieldSize = coordinates.size.toSize()
                            },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { 
                                    searchText = "" 
                                    expanded = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    DropdownMenu(
                        expanded = expanded && filteredExercises.isNotEmpty(),
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                            .heightIn(max = 200.dp)
                            .background(Color(0xFF2C2C2C)),
                        properties = androidx.compose.ui.window.PopupProperties(focusable = false)
                    ) {
                        filteredExercises.forEach { exercise ->
                            DropdownMenuItem(
                                text = { Text(text = exercise, color = Color.White) },
                                onClick = {
                                    addedExercises.add(RoutineExercise(name = exercise))
                                    searchText = ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(addedExercises, key = { it.hashCode() }) { exercise ->
                AddedExerciseCard(
                    exercise = exercise, 
                    onExerciseUpdate = { updatedExercise ->
                        val index = addedExercises.indexOfFirst { it.name == updatedExercise.name }
                        if (index != -1) {
                            addedExercises[index] = updatedExercise
                        }
                    }, 
                    onRemoveExercise = {
                        addedExercises.remove(exercise)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        Column(modifier = Modifier.padding(12.dp)) { // Reduced padding
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name, 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between headers
            ) {
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between set items
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
                    }, modifier = Modifier.weight(1f))

                    CompactTextField(value = set.reps, onValueChange = {
                        val newSets = exercise.sets.toMutableList()
                        newSets[index] = newSets[index].copy(reps = it)
                        onExerciseUpdate(exercise.copy(sets = newSets))
                    }, modifier = Modifier.weight(1f))

                    // This is the small delete button for each individual set. This is correct and stays.
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
