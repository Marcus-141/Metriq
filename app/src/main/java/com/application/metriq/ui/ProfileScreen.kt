package com.application.metriq.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.data.SessionManager
import com.application.metriq.ui.theme.MetriqTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    var isEditing by remember { mutableStateOf(false) }

    val weight by sessionManager.weight.collectAsState(initial = "")
    val height by sessionManager.height.collectAsState(initial = "")
    val age by sessionManager.age.collectAsState(initial = "")
    val gender by sessionManager.gender.collectAsState(initial = "")
    val activityLevel by sessionManager.activityLevel.collectAsState(initial = "")
    val protein by sessionManager.protein.collectAsState(initial = "")
    val bmr by sessionManager.bmr.collectAsState(initial = "")
    val tee by sessionManager.tee.collectAsState(initial = "")

    var tempWeight by remember { mutableStateOf(weight) }
    var tempHeight by remember { mutableStateOf(height) }
    var tempAge by remember { mutableStateOf(age) }
    var tempGender by remember { mutableStateOf(gender) }
    var tempActivityLevel by remember { mutableStateOf(activityLevel) }

    // Update temporary states when the collected states change
    LaunchedEffect(weight, height, age, gender, activityLevel) {
        tempWeight = weight
        tempHeight = height
        tempAge = age
        tempGender = gender
        tempActivityLevel = activityLevel
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = Color.Black) {
                IconButton(onClick = { navController.navigate("dashboard") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Home, contentDescription = "Dashboard", tint = Color.White)
                }
                IconButton(onClick = { navController.navigate("tracking") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Tracking", tint = Color.White)
                }
                IconButton(onClick = { navController.navigate("profile") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text(
                    text = "Your Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ProfileInfoCard(
                    isEditing = isEditing,
                    weight = if (isEditing) tempWeight else weight,
                    height = if (isEditing) tempHeight else height,
                    age = if (isEditing) tempAge else age,
                    gender = if (isEditing) tempGender else gender,
                    activityLevel = if (isEditing) tempActivityLevel else activityLevel,
                    onWeightChange = { tempWeight = it },
                    onHeightChange = { tempHeight = it },
                    onAgeChange = { tempAge = it },
                    onGenderChange = { tempGender = it },
                    onActivityLevelChange = { tempActivityLevel = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                GoalsCard(
                    protein = protein,
                    bmr = bmr,
                    tee = tee
                )

                Spacer(modifier = Modifier.weight(1f))

                EditSaveButton(
                    isEditing = isEditing,
                    onClick = {
                        if (isEditing) {
                            scope.launch {
                                val weightValue = tempWeight.toDoubleOrNull() ?: 0.0
                                val heightValue = tempHeight.toDoubleOrNull() ?: 0.0
                                val ageValue = tempAge.toIntOrNull() ?: 0
                                
                                val bmrValue = if (tempGender == "Male") {
                                    (10 * weightValue) + (6.25 * heightValue) - (5 * ageValue) + 5
                                } else {
                                    (10 * weightValue) + (6.25 * heightValue) - (5 * ageValue) - 161
                                }

                                val activityMultiplier = when (tempActivityLevel) {
                                    "Sedentary" -> 1.2
                                    "Lightly Active" -> 1.375
                                    "Moderately Active" -> 1.55
                                    "Very Active" -> 1.725
                                    "Extra Active" -> 1.9
                                    else -> 1.2
                                }

                                val teeValue = bmrValue * activityMultiplier
                                val proteinValue = (weightValue * 1.8).toInt().toString()

                                sessionManager.saveProfileData(
                                    tempWeight, 
                                    tempHeight, 
                                    tempAge, 
                                    tempGender, 
                                    tempActivityLevel,
                                    proteinValue,
                                    bmrValue.toInt().toString(),
                                    teeValue.toInt().toString()
                                )
                            }
                        }
                        isEditing = !isEditing
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoCard(
    isEditing: Boolean,
    weight: String,
    height: String,
    age: String,
    gender: String,
    activityLevel: String,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onActivityLevelChange: (String) -> Unit
) {
    val genderOptions = listOf("Male", "Female")
    val activityOptions = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extra Active")
    var genderExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (isEditing) {
                EditableProfileRow(label = "Weight (kg)", value = weight, onValueChange = onWeightChange)
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
                EditableProfileRow(label = "Height (cm)", value = height, onValueChange = onHeightChange)
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
                EditableProfileRow(label = "Age", value = age, onValueChange = onAgeChange)
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
                
                // Gender Dropdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Gender", color = Color.LightGray, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {}, 
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            genderOptions.forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = {
                                    onGenderChange(it)
                                    genderExpanded = false
                                })
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))

                // Activity Level Dropdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Activity Level", color = Color.LightGray, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    ExposedDropdownMenuBox(
                        expanded = activityExpanded,
                        onExpandedChange = { activityExpanded = !activityExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = activityLevel,
                            onValueChange = {}, 
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = activityExpanded,
                            onDismissRequest = { activityExpanded = false }
                        ) {
                            activityOptions.forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = {
                                    onActivityLevelChange(it)
                                    activityExpanded = false
                                })
                            }
                        }
                    }
                }

            } else {
                ProfileDetailRow(label = "Weight", value = "$weight kg")
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
                ProfileDetailRow(label = "Height", value = "$height cm")
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
                ProfileDetailRow(label = "Age", value = age)
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
                ProfileDetailRow(label = "Gender", value = gender)
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
                ProfileDetailRow(label = "Activity Level", value = activityLevel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableProfileRow(label: String, value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 16.sp, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@Composable
fun GoalsCard(
    protein: String,
    bmr: String,
    tee: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileDetailRow(label = "Protein Requirements", value = "$protein g")
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
            ProfileDetailRow(label = "Basal Metabolic Rate (BMR)", value = "$bmr kCal")
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f))
            ProfileDetailRow(label = "Total Energy Expenditure (TEE)", value = "$tee kCal")
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 16.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun EditSaveButton(isEditing: Boolean, onClick: () -> Unit) {
    val buttonColor = if (isEditing) Color.Green else MaterialTheme.colorScheme.primary
    val buttonText = if (isEditing) "Save Data" else "Edit Data"

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = buttonColor),
        border = BorderStroke(1.dp, buttonColor)
    ) {
        Text(buttonText)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MetriqTheme {
        ProfileScreen(navController = rememberNavController())
    }
}
