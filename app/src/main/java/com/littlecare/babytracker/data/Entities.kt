package com.littlecare.babytracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ActivityType {
    FEED, POOP, PEE
}

enum class FeedSubType(val label: String) {
    BREAST("Breast"),
    BOTTLE("Bottle"),
    FORMULA("Formula")
}

enum class PoopConsistency(val label: String) {
    NORMAL("Normal"),
    LOOSE("Loose / Watery"),
    HARD("Hard / Pellets"),
    MUCOUSY("Mucousy")
}

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val timestampMillis: Long,
    val durationMinutes: Int = 0,
    val subType: String = "",
    val note: String = ""
)

@Entity(tableName = "growth_entries")
data class GrowthEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekNumber: Int,
    val recordedDateEpochDay: Long,
    val weightKg: Float?,
    val heightCm: Float?
)

@Entity(tableName = "vaccine_records")
data class VaccineRecord(
    @PrimaryKey val code: String,
    val name: String,
    val stageLabel: String,
    val daysAfterBirth: Int,
    val dueDateEpochDay: Long,
    val description: String,
    val isAdministered: Boolean = false,
    val administeredDateEpochDay: Long? = null
)
