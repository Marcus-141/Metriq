package com.application.metriq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.application.metriq.ui.theme.CardBackground
import com.application.metriq.ui.theme.TextGray
import com.application.metriq.ui.theme.TextWhite
import com.application.metriq.viewmodel.ProfileViewModel
import com.application.metriq.viewmodel.UserProfile
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val userProfile by viewModel.userProfile.collectAsState()
    var isInEditMode by remember { mutableStateOf(false) }

    var editableProfile by remember(userProfile, isInEditMode) {
        mutableStateOf(userProfile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "User Profile",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            if (isInEditMode) {
                EditProfileView(
                    profile = editableProfile,
                    onProfileChange = { editableProfile = it },
                    onSave = {
                        viewModel.saveProfileAndCalculateGoals(editableProfile)
                        isInEditMode = false
                    },
                    onCancel = { isInEditMode = false }
                )
            } else {
                DisplayProfileView(
                    profile = userProfile,
                    onEdit = { isInEditMode = true }
                )
            }
        }
    }
}

@Composable
fun DisplayProfileView(profile: UserProfile, onEdit: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        ProfileRow("Weight", "${profile.weight} kg")
        ProfileRow("Height", "${profile.height} cm")
        ProfileRow("Age", profile.age)
        ProfileRow("Gender", profile.gender)
        ProfileRow("Activity Level", profile.activityLevel)
        ProfileRow("Fitness Goal", profile.fitnessGoal)
        ProfileRow("Calorie Goal", "${profile.calorieGoal} kcal", isGoal = true)
        
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Profile", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileView(
    profile: UserProfile,
    onProfileChange: (UserProfile) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = profile.weight,
            onValueChange = { onProfileChange(profile.copy(weight = it)) },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = profile.height,
            onValueChange = { onProfileChange(profile.copy(height = it)) },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = profile.age,
            onValueChange = { onProfileChange(profile.copy(age = it)) },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(12.dp))

        Dropdown(
            label = "Gender",
            options = listOf("Male", "Female"),
            selected = profile.gender,
            onSelected = { onProfileChange(profile.copy(gender = it)) }
        )
        
        Spacer(Modifier.height(12.dp))

        Dropdown(
            label = "Activity Level",
            options = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extra Active"),
            selected = profile.activityLevel,
            onSelected = { onProfileChange(profile.copy(activityLevel = it)) }
        )
        
        Spacer(Modifier.height(12.dp))

        Dropdown(
            label = "Fitness Goal",
            options = listOf("Maintain Weight", "Lose Fat", "Gain Muscle"),
            selected = profile.fitnessGoal,
            onSelected = { onProfileChange(profile.copy(fitnessGoal = it)) }
        )

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(Modifier.width(16.dp))
            Button(onClick = onSave) {
                Text("Save")
            }
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String, isGoal: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextGray, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            text = value,
            color = if (isGoal) MaterialTheme.colorScheme.primary else TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
