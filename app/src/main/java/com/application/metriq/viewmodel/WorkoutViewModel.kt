package com.application.metriq.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.application.metriq.data.AppDatabase
import com.application.metriq.data.ExerciseTargetMapper
import com.application.metriq.data.FrontMuscleGroup
import com.application.metriq.data.RearMuscleGroup
import com.application.metriq.data.entity.ExerciseJson
import com.application.metriq.data.RoutineExercise
import com.application.metriq.data.entity.WorkoutLog
import com.application.metriq.data.WorkoutRoutine
import com.application.metriq.data.loadExercisesFromAssets
import com.application.metriq.ui.components.DataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val workoutRoutineDao = AppDatabase.getDatabase(application).workoutRoutineDao()
    private val workoutLogDao = AppDatabase.getDatabase(application).workoutLogDao()
    
    private val _allExercises = MutableStateFlow<List<ExerciseJson>>(emptyList())
    val allExercises: StateFlow<List<ExerciseJson>> = _allExercises.asStateFlow()

    val routines: StateFlow<List<WorkoutRoutine>> = workoutRoutineDao.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutHistory: StateFlow<List<WorkoutLog>> = workoutLogDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _frontBodyScores = MutableStateFlow<Map<FrontMuscleGroup, Float>>(emptyMap())
    val frontBodyScores: StateFlow<Map<FrontMuscleGroup, Float>> = _frontBodyScores.asStateFlow()

    private val _rearBodyScores = MutableStateFlow<Map<RearMuscleGroup, Float>>(emptyMap())
    val rearBodyScores: StateFlow<Map<RearMuscleGroup, Float>> = _rearBodyScores.asStateFlow()

    private val _workoutDuration = MutableStateFlow(0L)
    val workoutDuration: StateFlow<Long> = _workoutDuration.asStateFlow()

    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()

    private var timerJob: Job? = null

    private var isSaving = false

    val totalVolumeOverTime: StateFlow<List<DataPoint>> = workoutHistory.map { logs ->
        logs.groupBy { log ->
            Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        }.map { (date, dailyLogs) ->
            val dailyVolume = dailyLogs.sumOf { log ->
                log.exercises.sumOf { exercise ->
                    exercise.sets.sumOf { set ->
                        set.weight.toString().toDoubleOrNull() ?: 0.0

                    }
                }
            }
            DataPoint(date, dailyVolume.toFloat())
        }.sortedBy { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyVolumeData: StateFlow<List<Pair<String, Float>>> = workoutHistory.map { logs ->
        val now = LocalDate.now()
        val eightWeeksAgo = now.minusWeeks(8)
        
        val logsInPeriod = logs.filter { 
            val date = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            !date.isBefore(eightWeeksAgo) && !date.isAfter(now)
        }

        val weeklyVolumes = mutableMapOf<LocalDate, Double>()
        // Initialize last 8 weeks with 0
        for (i in 0 until 8) {
            val weekStart = now.minusWeeks(i.toLong()).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            weeklyVolumes[weekStart] = 0.0
        }

        logsInPeriod.forEach { log ->
            val date = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            
            if (weeklyVolumes.containsKey(weekStart)) {
                val volume = log.exercises.sumOf { exercise ->
                    exercise.sets.sumOf { set ->
                        set.weight.toString().toDoubleOrNull() ?: 0.0

                    }
                }
                weeklyVolumes[weekStart] = weeklyVolumes[weekStart]!! + volume
            }
        }

        val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
        weeklyVolumes.entries.sortedBy { it.key }.map { (date, volume) ->
            date.format(formatter) to volume.toFloat()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getMaxWeightForExercise(exerciseName: String): StateFlow<List<DataPoint>> {
        return workoutHistory.map { logs ->
            logs.flatMap { log ->
                log.exercises
                    .filter { it.name.equals(exerciseName, ignoreCase = true) }
                    .map { exercise ->
                        val maxWeight = exercise.sets.maxOfOrNull {
                            it.weight.toString().toDoubleOrNull() ?: 0.0
                        } ?: 0.0
                        val date = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                        DataPoint(date, maxWeight.toFloat())
                    }
            }
            .groupBy { it.date } // In case there are multiple entries for same day, take max
            .map { (date, points) ->
                DataPoint(date, points.maxOf { it.value })
            }
            .sortedBy { it.date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _allExercises.value = loadExercisesFromAssets(getApplication())
                ExerciseTargetMapper.loadExercises(getApplication())
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error loading exercises", e)
                _allExercises.value = emptyList()
            }
        }

        // Observe workoutHistory and update recovery status
        viewModelScope.launch {
            workoutHistory.collect { logs ->
                calculateRecoveryStatus(logs)
            }
        }
    }

    private fun calculateRecoveryStatus(logs: List<WorkoutLog>) {
        val frontScores = mutableMapOf<FrontMuscleGroup, Float>()
        val rearScores = mutableMapOf<RearMuscleGroup, Float>()
        val now = LocalDateTime.now()

        logs.forEach { log ->

            val logDateTime = java.time.Instant.ofEpochMilli(log.timestamp)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()

            val hours = ChronoUnit.HOURS.between(logDateTime, now)
            
            // Decay Logic
            val score = when {
                hours < 24 -> 1.0f // Red (Strained)
                hours < 48 -> 0.6f // Yellow (Fatigued)
                hours < 72 -> 0.3f // Green (Recovered)
                else -> 0.0f       // Fresh
            }

            if (score > 0) {
                // For each exercise in the log, update muscle scores
                log.exercises.forEach { exercise ->
                    val frontTargets = ExerciseTargetMapper.getFrontTargets(exercise.name)
                    frontTargets.forEach { muscle ->
                        val current = frontScores[muscle] ?: 0f
                        if (score > current) {
                            frontScores[muscle] = score
                        }
                    }

                    val rearTargets = ExerciseTargetMapper.getRearTargets(exercise.name)
                    rearTargets.forEach { muscle ->
                        val current = rearScores[muscle] ?: 0f
                        if (score > current) {
                            rearScores[muscle] = score
                        }
                    }
                }
            }
        }

        _frontBodyScores.value = frontScores
        _rearBodyScores.value = rearScores
    }

    fun startWorkoutFromRoutine(routineName: String) {
        _isWorkoutActive.value = true
        _workoutDuration.value = 0L
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (true) {
                delay(1000)
                _workoutDuration.value = (System.currentTimeMillis() - startTime) / 1000
            }
        }
    }

    fun saveWorkout(log: WorkoutLog) {
        // Stop if a save is already happening
        if (isSaving) return
        isSaving = true

        _isWorkoutActive.value = false
        timerJob?.cancel()

        // Ensure the log has the correct duration
        val finalLog = log.copy(duration = _workoutDuration.value)

        viewModelScope.launch {
            workoutLogDao.insert(finalLog)
            Log.d("WorkoutViewModel", "Finished and saved workout: $finalLog")

            // Allow saving again after this operation is done
            isSaving = false
        }
    }
    
    fun cancelWorkout() {
        _isWorkoutActive.value = false
        timerJob?.cancel()
        _workoutDuration.value = 0L
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


    fun deleteWorkoutLog(log: WorkoutLog) {
        viewModelScope.launch {
            workoutLogDao.delete(log)
        }
    }
}
