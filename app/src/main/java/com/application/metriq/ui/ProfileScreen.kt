package com.application.metriq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.application.metriq.data.UserProfile
import com.application.metriq.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    
    // Local state for editing to avoid jumpy inputs while typing
    var age by remember(userProfile.age) { mutableStateOf(userProfile.age.toString()) }
    var height by remember(userProfile.height) { mutableStateOf(userProfile.height.toString()) }
    var weight by remember(userProfile.weight) { mutableStateOf(userProfile.weight.toString()) }
    
    // We update the viewmodel when focus is lost or done action is pressed, 
    // or simply update on every change but formatted safely.
    // For simplicity, let's update immediately but handle parsing errors.

    fun updateProfile() {
        val newAge = age.toIntOrNull() ?: userProfile.age
        val newHeight = height.toDoubleOrNull() ?: userProfile.height
        val newWeight = weight.toDoubleOrNull() ?: userProfile.weight
        
        viewModel.updateUserProfile(
            userProfile.copy(
                age = newAge,
                height = newHeight,
                weight = newWeight
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Your Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = age,
            onValueChange = { 
                age = it
                updateProfile()
            },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        DropdownSelector(
            label = "Gender",
            options = listOf("Male", "Female"),
            selectedOption = userProfile.gender,
            onOptionSelected = { viewModel.updateUserProfile(userProfile.copy(gender = it)) }
        )

        OutlinedTextField(
            value = height,
            onValueChange = { 
                height = it
                updateProfile()
            },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { 
                weight = it
                updateProfile()
            },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        DropdownSelector(
            label = "Activity Level",
            options = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extra Active"),
            selectedOption = userProfile.activityLevel,
            onOptionSelected = { viewModel.updateUserProfile(userProfile.copy(activityLevel = it)) }
        )

        DropdownSelector(
            label = "Fitness Goal",
            options = listOf("Lose Fat", "Maintain Weight", "Gain Muscle"),
            selectedOption = userProfile.fitnessGoal,
            onOptionSelected = { viewModel.updateUserProfile(userProfile.copy(fitnessGoal = it)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedOption,
            onValueChange = { },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
