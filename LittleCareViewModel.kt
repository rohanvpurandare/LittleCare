package com.littlecare.babytracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.littlecare.babytracker.data.ActivityLog
import com.littlecare.babytracker.data.BabyProfile
import com.littlecare.babytracker.data.GrowthEntry
import com.littlecare.babytracker.data.LittleCareDatabase
import com.littlecare.babytracker.data.MilestoneRecord
import com.littlecare.babytracker.data.UserPrefs
import com.littlecare.babytracker.data.VaccineRecord
import com.littlecare.babytracker.domain.IndianVaccineSchedule
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class LittleCareViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = LittleCareDatabase.getDatabase(application).babyDao()
    private val prefs = UserPrefs(application)

    val profiles: StateFlow<List<BabyProfile>> = dao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nightVisionMode: StateFlow<Boolean> = prefs.nightVisionMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val biometricLockEnabled: StateFlow<Boolean> = prefs.biometricLockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val activeBaby: StateFlow<BabyProfile?> = combine(profiles, prefs.activeBabyId) { list, savedId ->
        list.find { it.id == savedId } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val activityLogs: StateFlow<List<ActivityLog>> = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(emptyList()) else dao.getActivityLogs(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val growthEntries: StateFlow<List<GrowthEntry>> = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(emptyList()) else dao.getGrowthEntries(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val vaccines: StateFlow<List<VaccineRecord>> = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(emptyList()) else dao.getVaccines(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val milestones: StateFlow<List<MilestoneRecord>> = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(emptyList()) else dao.getMilestones(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Feeding / Sleep Timer State
    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _timerLabel = MutableStateFlow("Breast (Left)")
    val timerLabel: StateFlow<String> = _timerLabel.asStateFlow()

    private var timerJob: Job? = null

    fun selectTimerLabel(label: String) {
        _timerLabel.value = label
    }

    fun toggleTimer() {
        if (_timerRunning.value) {
            _timerRunning.value = false
            timerJob?.cancel()
        } else {
            _timerRunning.value = true
            timerJob = viewModelScope.launch {
                while (_timerRunning.value) {
                    delay(1000L)
                    _timerSeconds.value += 1
                }
            }
        }
    }

    fun saveAndResetTimer() {
        val baby = activeBaby.value ?: return
        val elapsed = _timerSeconds.value
        _timerRunning.value = false
        timerJob?.cancel()
        _timerSeconds.value = 0
        if (elapsed > 0) {
            val isSleep = _timerLabel.value.contains("Sleep", ignoreCase = true)
            viewModelScope.launch {
                dao.insertActivity(
                    ActivityLog(
                        babyId = baby.id,
                        category = if (isSleep) "SLEEP" else "FEED",
                        subType = _timerLabel.value,
                        durationSeconds = elapsed
                    )
                )
            }
        }
    }

    fun resetTimerOnly() {
        _timerRunning.value = false
        timerJob?.cancel()
        _timerSeconds.value = 0
    }

    fun createBabyProfile(
        name: String,
        birthDate: LocalDate,
        gender: String,
        birthWeightKg: Float,
        birthHeightCm: Float
    ) {
        viewModelScope.launch {
            val epochDay = birthDate.toEpochDay()
            val id = dao.insertProfile(
                BabyProfile(
                    name = name.trim(),
                    birthDateEpochDay = epochDay,
                    gender = gender,
                    birthWeightKg = birthWeightKg,
                    birthHeightCm = birthHeightCm
                )
            )
            // Seed Week 0 birth measurement
            dao.insertGrowth(
                GrowthEntry(
                    babyId = id,
                    weekNumber = 0,
                    weightKg = birthWeightKg,
                    heightCm = birthHeightCm,
                    recordedEpochDay = epochDay
                )
            )
            // Seed Indian Immunization Schedule & Milestones
            dao.insertVaccines(IndianVaccineSchedule.generateForBaby(id))
            dao.insertMilestones(IndianVaccineSchedule.defaultMilestonesForBaby(id))
            prefs.setActiveBabyId(id)
        }
    }

    fun selectActiveBaby(id: Long) {
        viewModelScope.launch { prefs.setActiveBabyId(id) }
    }

    fun logQuickActivity(category: String, subType: String, amountMl: Int = 0, durationSeconds: Int = 0, notes: String = "") {
        val baby = activeBaby.value ?: return
        viewModelScope.launch {
            dao.insertActivity(
                ActivityLog(
                    babyId = baby.id,
                    category = category,
                    subType = subType,
                    amountMl = amountMl,
                    durationSeconds = durationSeconds,
                    notes = notes
                )
            )
        }
    }

    fun deleteActivity(log: ActivityLog) {
        viewModelScope.launch { dao.deleteActivity(log) }
    }

    fun addGrowthEntry(weekNumber: Int, weightKg: Float, heightCm: Float, headCm: Float = 0f) {
        val baby = activeBaby.value ?: return
        viewModelScope.launch {
            dao.insertGrowth(
                GrowthEntry(
                    babyId = baby.id,
                    weekNumber = weekNumber.coerceIn(0, 52),
                    weightKg = weightKg,
                    heightCm = heightCm,
                    headCircumferenceCm = headCm,
                    recordedEpochDay = LocalDate.now().toEpochDay()
                )
            )
        }
    }

    fun deleteGrowthEntry(entry: GrowthEntry) {
        viewModelScope.launch { dao.deleteGrowth(entry) }
    }

    fun toggleVaccineCompleted(vaccine: VaccineRecord) {
        viewModelScope.launch {
            val nowCompleted = !vaccine.isCompleted
            dao.updateVaccine(
                vaccine.copy(
                    isCompleted = nowCompleted,
                    completedEpochDay = if (nowCompleted) LocalDate.now().toEpochDay() else null
                )
            )
        }
    }

    fun toggleMilestone(milestone: MilestoneRecord, note: String = milestone.memoryNote) {
        viewModelScope.launch {
            val achieved = !milestone.isAchieved
            dao.updateMilestone(
                milestone.copy(
                    isAchieved = achieved,
                    achievedEpochDay = if (achieved) LocalDate.now().toEpochDay() else null,
                    memoryNote = note
                )
            )
        }
    }

    fun toggleNightVision(enabled: Boolean) {
        viewModelScope.launch { prefs.setNightVisionMode(enabled) }
    }

    fun toggleBiometricLock(enabled: Boolean) {
        viewModelScope.launch { prefs.setBiometricLock(enabled) }
    }
}
