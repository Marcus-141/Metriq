package com.application.metriq.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.application.metriq.data.AppDatabase
import com.application.metriq.data.entity.ExerciseJson
import com.application.metriq.data.entity.RoutineExercise
import com.application.metriq.data.entity.WorkoutRoutine
import com.application.metriq.data.loadExercisesFromAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val workoutRoutineDao = AppDatabase.getDatabase(application).workoutRoutineDao()
    
    private val _allExercises = MutableStateFlow<List<ExerciseJson>>(emptyList())
    val allExercises: StateFlow<List<ExerciseJson>> = _allExercises.asStateFlow()

    val routines: StateFlow<List<WorkoutRoutine>> = workoutRoutineDao.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _allExercises.value = loadExercisesFromAssets(getApplication())
        }
    }

    fun addRoutine(name: String, exercises: List<RoutineExercise>) {
        viewModelScope.launch {
            val newRoutine = WorkoutRoutine(name = name, exercises = exercises)
            workoutRoutineDao.insert(newRoutine)
        }
    }

    fun updateRoutine(routine: WorkoutRoutine) {
        viewModelScope.launch {
            workoutRoutineDao.update(routine)
        }
    }
    
    fun deleteRoutine(routine: WorkoutRoutine) {
        viewModelScope.launch {
            workoutRoutineDao.delete(routine)
        }
    }
}
