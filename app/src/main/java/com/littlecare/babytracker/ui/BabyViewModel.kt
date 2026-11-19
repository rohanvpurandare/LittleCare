package com.littlecare.babytracker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.littlecare.babytracker.data.ActivityDao
import com.littlecare.babytracker.data.ActivityLog
import com.littlecare.babytracker.data.ActivityType
import com.littlecare.babytracker.data.BabyPreferences
import com.littlecare.babytracker.data.BabyProfile
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

    val babyProfile: StateFlow<BabyProfile?> = preferences.babyProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val startOfTodayMillis: Long
        get() = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    val todayLogs: StateFlow<List<ActivityLog>> = activityDao.getLogsSince(startOfTodayMillis)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<ActivityLog>> = activityDao.getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val growthEntries: StateFlow<List<GrowthEntry>> = growthDao.getAllGrowthEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaccineRecords: StateFlow<List<VaccineRecord>> = vaccineDao.getAllVaccines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _lastExportResult = MutableStateFlow<ExportResult?>(null)
    val lastExportResult: StateFlow<ExportResult?> = _lastExportResult.asStateFlow()

    fun clearExportResult() {
        _lastExportResult.value = null
    }

    fun completeOnboarding(
        name: String,
        birthDateEpochDay: Long,
        birthTimeText: String,
        birthWeightKg: Float? = null,
        birthHeightCm: Float? = null
    ) {
        viewModelScope.launch {
            preferences.saveBabyProfile(name, birthDateEpochDay, birthTimeText)
            val initialVaccines = IndianVaccineSchedule.buildScheduleForBirthDate(birthDateEpochDay)
            vaccineDao.insertAll(initialVaccines)
            if (birthWeightKg != null || birthHeightCm != null) {
                growthDao.insertGrowthEntry(
                    GrowthEntry(
                        weekNumber = 0,
                        recordedDateEpochDay = birthDateEpochDay,
                        weightKg = birthWeightKg,
                        heightCm = birthHeightCm
                    )
                )
            }
        }
    }

    fun logPeeInstant(onLogged: (ActivityLog) -> Unit = {}) {
        viewModelScope.launch {
            val log = ActivityLog(
                type = ActivityType.PEE.name,
                timestampMillis = System.currentTimeMillis()
            )
            val id = activityDao.insertLog(log)
            onLogged(log.copy(id = id))
        }
    }

    fun logPoop(consistency: PoopConsistency, note: String, timestampMillis: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            activityDao.insertLog(
                ActivityLog(
                    type = ActivityType.POOP.name,
                    timestampMillis = timestampMillis,
                    subType = consistency.label,
                    note = note.trim()
                )
            )
        }
    }

    fun logFeed(feedType: FeedSubType, durationMinutes: Int, startTimeMillis: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            activityDao.insertLog(
                ActivityLog(
                    type = ActivityType.FEED.name,
                    timestampMillis = startTimeMillis,
                    durationMinutes = durationMinutes.coerceAtLeast(1),
                    subType = feedType.label
                )
            )
        }
    }

    fun deleteActivityLog(log: ActivityLog) {
        viewModelScope.launch {
            activityDao.deleteLog(log)
        }
    }

    fun addGrowthEntry(weekNumber: Int, weightKg: Float?, heightCm: Float?) {
        if (weightKg == null && heightCm == null) return
        viewModelScope.launch {
            growthDao.insertGrowthEntry(
                GrowthEntry(
                    weekNumber = weekNumber.coerceIn(0, 52),
                    recordedDateEpochDay = LocalDate.now().toEpochDay(),
                    weightKg = weightKg,
                    heightCm = heightCm
                )
            )
        }
    }

    fun deleteGrowthEntry(entry: GrowthEntry) {
        viewModelScope.launch {
            growthDao.deleteGrowthEntry(entry)
        }
    }

    fun toggleVaccineAdministered(code: String, isAdministered: Boolean, administeredEpochDay: Long?) {
        viewModelScope.launch {
            vaccineDao.updateVaccineStatus(code, isAdministered, administeredEpochDay)
        }
    }

    fun exportToExcel(context: Context, profile: BabyProfile) {
        if (_isExporting.value) return
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val allLogs = activityDao.getAllLogsSnapshot()
                val res = ReportExporter.exportExcelWorkbook(
                    context = context,
                    profile = profile,
                    logs = allLogs,
                    growthEntries = growthEntries.value,
                    vaccines = vaccineRecords.value
                )
                _lastExportResult.value = res
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportToPdf(context: Context, profile: BabyProfile) {
        if (_isExporting.value) return
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val allLogs = activityDao.getAllLogsSnapshot()
                val res = ReportExporter.exportPdfReport(
                    context = context,
                    profile = profile,
                    logs = allLogs,
                    growthEntries = growthEntries.value,
                    vaccines = vaccineRecords.value
                )
                _lastExportResult.value = res
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun calculateBabyAgeWeeks(birthDateEpochDay: Long): Int {
        if (birthDateEpochDay <= 0L) return 0
        val dob = LocalDate.ofEpochDay(birthDateEpochDay)
        val days = ChronoUnit.DAYS.between(dob, LocalDate.now()).coerceAtLeast(0)
        return (days / 7).toInt().coerceAtMost(52)
    }

    class Factory(
        private val preferences: BabyPreferences,
        private val activityDao: ActivityDao,
        private val growthDao: GrowthDao,
        private val vaccineDao: VaccineDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BabyViewModel(preferences, activityDao, growthDao, vaccineDao) as T
        }
    }
}
