package com.application.metriq.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.application.metriq.data.AppDatabase
import com.application.metriq.data.entity.RoutineExercise
import com.application.metriq.data.entity.WorkoutRoutine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val workoutRoutineDao = AppDatabase.getDatabase(application).workoutRoutineDao()

    val routines: StateFlow<List<WorkoutRoutine>> = workoutRoutineDao.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRoutine(name: String, exercises: List<RoutineExercise>) {
        viewModelScope.launch {
            // The WorkoutRoutine entity expects a MutableList, so we convert the incoming list.
            val newRoutine = WorkoutRoutine(name = name, exercises = exercises.toMutableList())
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
