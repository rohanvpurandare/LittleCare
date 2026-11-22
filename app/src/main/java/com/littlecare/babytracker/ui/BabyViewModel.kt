package com.littlecare.babytracker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.littlecare.babytracker.data.ActivityDao
import com.littlecare.babytracker.data.ActivityLog
import com.littlecare.babytracker.data.ActivityType
import com.littlecare.babytracker.data.BabyPreferences
import com.littlecare.babytracker.data.BabyProfilePref
import com.littlecare.babytracker.data.FeedSubType
import com.littlecare.babytracker.data.GrowthDao
import com.littlecare.babytracker.data.GrowthEntry
import com.littlecare.babytracker.data.PoopConsistency
import com.littlecare.babytracker.data.VaccineDao
import com.littlecare.babytracker.data.VaccineRecord
import com.littlecare.babytracker.domain.IndianVaccineSchedule
import com.littlecare.babytracker.export.ExportResult
import com.littlecare.babytracker.export.ReportExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class BabyViewModel(
    private val preferences: BabyPreferences,
    private val activityDao: ActivityDao,
    private val growthDao: GrowthDao,
    private val vaccineDao: VaccineDao
) : ViewModel() {

    val babyProfile: StateFlow<BabyProfilePref?> = preferences.babyProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _logsForSelectedDate = MutableStateFlow<List<ActivityLog>>(emptyList())
    val logsForSelectedDate: StateFlow<List<ActivityLog>> = _logsForSelectedDate.asStateFlow()

    private val _allGrowthEntries = MutableStateFlow<List<GrowthEntry>>(emptyList())
    val allGrowthEntries: StateFlow<List<GrowthEntry>> = _allGrowthEntries.asStateFlow()

    private val _allVaccineRecords = MutableStateFlow<List<VaccineRecord>>(emptyList())
    val allVaccineRecords: StateFlow<List<VaccineRecord>> = _allVaccineRecords.asStateFlow()

    private val _exportResult = MutableStateFlow<ExportResult?>(null)
    val exportResult: StateFlow<ExportResult?> = _exportResult.asStateFlow()

    init {
        loadLogsForDate(_selectedDate.value)
        loadGrowthEntries()
        loadVaccineRecords()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        loadLogsForDate(date)
    }

    private fun loadLogsForDate(date: LocalDate) {
        viewModelScope.launch {
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            activityDao.getLogsBetween(startOfDay, endOfDay).collect { logs ->
                _logsForSelectedDate.value = logs
            }
        }
    }

    private fun loadGrowthEntries() {
        viewModelScope.launch {
            growthDao.getAllEntries().collect { entries ->
                _allGrowthEntries.value = entries
            }
        }
    }

    private fun loadVaccineRecords() {
        viewModelScope.launch {
            vaccineDao.getAllRecords().collect { records ->
                _allVaccineRecords.value = records
            }
        }
    }

    fun saveProfile(name: String, birthDateEpochDay: Long, birthTimeText: String) {
        viewModelScope.launch {
            preferences.saveBabyProfile(name, birthDateEpochDay, birthTimeText)
        }
    }

    fun logFeeding(
        type: FeedSubType,
        amountMl: Float? = null,
        durationMinutes: Int? = null,
        breastSide: String? = null,
        foodDescription: String? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            activityDao.insert(
                ActivityLog(
                    timestamp = System.currentTimeMillis(),
                    type = ActivityType.FEEDING,
                    feedSubType = type,
                    amountMl = amountMl,
                    durationMinutes = durationMinutes,
                    breastSide = breastSide,
                    foodDescription = foodDescription,
                    notes = notes
                )
            )
        }
    }

    fun logDiaper(
        isWet: Boolean,
        isDirty: Boolean,
        poopColor: String? = null,
        poopConsistency: PoopConsistency? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            activityDao.insert(
                ActivityLog(
                    timestamp = System.currentTimeMillis(),
                    type = ActivityType.DIAPER,
                    isWet = isWet,
                    isDirty = isDirty,
                    poopColor = poopColor,
                    poopConsistency = poopConsistency,
                    notes = notes
                )
            )
        }
    }

    fun logSleep(durationMinutes: Int, notes: String = "") {
        viewModelScope.launch {
            activityDao.insert(
                ActivityLog(
                    timestamp = System.currentTimeMillis(),
                    type = ActivityType.SLEEP,
                    durationMinutes = durationMinutes,
                    notes = notes
                )
            )
        }
    }

    fun logTummyTime(durationMinutes: Int, notes: String = "") {
        viewModelScope.launch {
            activityDao.insert(
                ActivityLog(
                    timestamp = System.currentTimeMillis(),
                    type = ActivityType.TUMMY_TIME,
                    durationMinutes = durationMinutes,
                    notes = notes
                )
            )
        }
    }

    fun deleteLog(log: ActivityLog) {
        viewModelScope.launch {
            activityDao.delete(log)
        }
    }

    fun addGrowthEntry(dateEpochDay: Long, weightKg: Float, heightCm: Float, headCircumferenceCm: Float? = null, notes: String = "") {
        viewModelScope.launch {
            growthDao.insert(
                GrowthEntry(
                    dateEpochDay = dateEpochDay,
                    weightKg = weightKg,
                    heightCm = heightCm,
                    headCircumferenceCm = headCircumferenceCm,
                    notes = notes
                )
            )
        }
    }

    fun toggleVaccineDone(vaccineName: String, isDone: Boolean) {
        viewModelScope.launch {
            val existing = vaccineDao.getRecordForVaccine(vaccineName)
            if (existing != null) {
                vaccineDao.update(existing.copy(isDone = isDone, dateAdministeredEpochDay = if (isDone) LocalDate.now().toEpochDay() else null))
            } else {
                vaccineDao.insert(
                    VaccineRecord(
                        vaccineName = vaccineName,
                        isDone = isDone,
                        dateAdministeredEpochDay = if (isDone) LocalDate.now().toEpochDay() else null
                    )
                )
            }
        }
    }

    fun exportPdfReport(context: Context) {
        viewModelScope.launch {
            val profile = babyProfile.value
            val logs = logsForSelectedDate.value
            val growth = allGrowthEntries.value
            val vaccines = allVaccineRecords.value
            _exportResult.value = ReportExporter.exportPdf(context, profile, logs, growth, vaccines)
        }
    }

    fun exportCsvReport(context: Context) {
        viewModelScope.launch {
            val logs = activityDao.getAllLogsSync()
            _exportResult.value = ReportExporter.exportCsv(context, logs)
        }
    }

    fun clearExportResult() {
        _exportResult.value = null
    }

    fun clearAllData() {
        viewModelScope.launch {
            preferences.clear()
            activityDao.deleteAll()
            growthDao.deleteAll()
            vaccineDao.deleteAll()
        }
    }
}

class BabyViewModelFactory(
    private val preferences: BabyPreferences,
    private val activityDao: ActivityDao,
    private val growthDao: GrowthDao,
    private val vaccineDao: VaccineDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BabyViewModel::class.java)) {
            return BabyViewModel(preferences, activityDao, growthDao, vaccineDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
