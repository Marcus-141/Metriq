package com.application.metriq.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.metriq.data.ExerciseSet
import com.application.metriq.data.RoutineExercise
import com.application.metriq.data.WorkoutRoutine
import com.application.metriq.data.entity.WorkoutLog
import com.application.metriq.ui.theme.PrimaryGreen
import com.application.metriq.ui.theme.WorkoutBlue
import com.application.metriq.viewmodel.WorkoutViewModel
import java.util.UUID

// Local UI state classes adapted for existing project structure
class UiWorkoutSet(
    val id: String = UUID.randomUUID().toString(), // Added unique ID for stable keys
    reps: String = "",
    weight: String = "",
    completed: Boolean = false
) {
    var reps by mutableStateOf(reps)
    var weight by mutableStateOf(weight)
    var completed by mutableStateOf(completed)
}

class ActiveWorkoutExercise(
    val name: String,
    initialSets: List<UiWorkoutSet>
) {
    var sets = initialSets.toMutableStateList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    routine: WorkoutRoutine?,
    onFinish: (WorkoutLog) -> Unit,
    onCancel: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    // ViewModel State
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsState()
    val durationSeconds by viewModel.workoutDuration.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    
    // Local State
    var workoutName by remember { mutableStateOf(routine?.name ?: "Quick Workout") }
    val exercises = remember(allExercises) { allExercises.map { it.name } }
    
    //Search state
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember{ mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    //Filter logic - Optimized with derivedStateOf
    val filteredExercises by remember {
        derivedStateOf {
            if (searchText.isBlank()) emptyList()
            else exercises.filter { it.contains(searchText, ignoreCase = true) }
        }
    }

    // Initialize exercises from routine
    val activeExercises = remember {
        routine?.exercises?.map { routineExpr ->
            ActiveWorkoutExercise(
                name = routineExpr.name,
                initialSets = routineExpr.sets.map {
                    UiWorkoutSet(reps = it.reps, weight = it.weight, completed = it.isCompleted)
                }
            )
        }?.toMutableStateList() ?: mutableStateListOf()
    }

    // Effect to start timer if entering with an active routine intent
    // Rely on user clicking "Start Workout" unless logic dictates auto-start.

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    BasicTextField(
                        value = workoutName,
                        onValueChange = { workoutName = it },
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(Color.White),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.Center) {
                                if (workoutName.isEmpty()) {
                                    Text(
                                        text = "Workout Name",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelWorkout()
                        onCancel()
                    }) { 
                        Icon(Icons.Default.ArrowBack, "Back") 
                    }
                },
                actions = {
                    IconButton(onClick = { /* Edit Mode */ }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentPadding = PaddingValues(16.dp)
            ) {
                if (!isWorkoutActive) {
                    Button(
                        onClick = {
                            viewModel.startWorkoutFromRoutine(workoutName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Start Workout", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            // Convert UI state back to Entity format
                            val finishedExercises = activeExercises.map { activeEx ->
                                RoutineExercise(
                                    name = activeEx.name,
                                    sets = activeEx.sets.map { uiSet ->
                                        ExerciseSet(
                                            reps = uiSet.reps, 
                                            weight = uiSet.weight, 
                                            isCompleted = uiSet.completed
                                        )
                                    }.toMutableList()
                                )
                            }
                            
                            val log = WorkoutLog(
                                name = workoutName,
                                timestamp = System.currentTimeMillis(),
                                duration = durationSeconds, // Will be overwritten by ViewModel but kept for structure
                                exercises = finishedExercises
                            )
                            viewModel.saveWorkout(log)
                            onFinish(log)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)), // Red
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Finish Workout", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Timer Card (Visible only when active)
            AnimatedVisibility(
                visible = isWorkoutActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatTime(durationSeconds),
                            style = MaterialTheme.typography.displayMedium,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(
                    items = activeExercises,
                    key = { _, exercise -> exercise.name } // Stable key for exercises
                ) { _, activeExercise ->
                    ExerciseCard(
                        activeExercise = activeExercise,
                        onAddSet = {
                            val lastSet = activeExercise.sets.lastOrNull()
                            activeExercise.sets.add(
                                UiWorkoutSet(weight = lastSet?.weight ?: "", reps = lastSet?.reps ?: "")
                            )
                        },
                        onRemoveSet = { setIdx ->
                            activeExercise.sets.removeAt(setIdx)
                        },
                        onRemoveExercise = {
                            activeExercises.remove(activeExercise)
                        }
                    )
                }

                item {
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
                                modifier = Modifier.padding(bottom = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                        .width(with(LocalDensity.current) { textFieldSize.width.toDp() } )
                                        .heightIn(max = 200.dp)
                                        .background(Color(0xFF2C2C2C)),
                                    properties = androidx.compose.ui.window.PopupProperties(focusable = false)
                                ) {
                                    filteredExercises.forEach { exercise ->
                                        DropdownMenuItem(
                                            text = { Text(text = exercise, color = Color.White) },
                                            onClick = {
                                                activeExercises.add(
                                                    ActiveWorkoutExercise(
                                                        name = exercise,
                                                        initialSets = listOf(UiWorkoutSet())
                                                    )
                                                )
                                                searchText = ""
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetRow(
    setNumber: Int,
    uiSet: UiWorkoutSet,
    onRemove: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$setNumber",
            modifier = Modifier.weight(0.5f),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        CompactTextField(
            value = uiSet.weight,
            onValueChange = { uiSet.weight = it },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )

        CompactTextField(
            value = uiSet.reps,
            onValueChange = { uiSet.reps = it },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )

        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, "Remove Set", tint = Color.Gray)
        }
    }
}

@Composable
fun ExerciseCard(
    activeExercise: ActiveWorkoutExercise,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onRemoveExercise: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeExercise.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onRemoveExercise) {
                    Icon(Icons.Default.Delete, "Remove Exercise", tint = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Header
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Set", Modifier.weight(0.5f), color = Color.Gray, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                Text("kg", Modifier.weight(1f), color = Color.Gray, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                Text("Reps", Modifier.weight(1f), color = Color.Gray, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(32.dp)) // For delete icon
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Sets
            activeExercise.sets.forEachIndexed { index, set ->
                key(set.id) {
                    SetRow(
                        setNumber = index + 1,
                        uiSet = set,
                        onRemove = { onRemoveSet(index) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddSet, 
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoutBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ Add Set", color = Color.White)
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
            .background(Color(0xFF121212), RoundedCornerShape(8.dp)),
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

fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
