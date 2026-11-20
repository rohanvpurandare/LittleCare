package com.littlecare.babytracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "baby_profiles")
data class BabyProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val birthDateEpochDay: Long,
    val gender: String = "Boy", // "Boy" or "Girl" for WHO reference curves
    val birthWeightKg: Float = 3.2f,
    val birthHeightCm: Float = 50.0f
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val category: String, // "FEED", "DIAPER", "SLEEP", "MEDS"
    val subType: String,  // e.g. "Breast (L)", "Breast (R)", "Bottle", "Wet", "Dirty", "Both", "Nap", "Night Sleep", "Drops"
    val amountMl: Int = 0,
    val durationSeconds: Int = 0,
    val notes: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "growth_entries")
data class GrowthEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val weekNumber: Int, // 0 to 52 (1st year weekly tracking)
    val weightKg: Float,
    val heightCm: Float,
    val headCircumferenceCm: Float = 0f,
    val recordedEpochDay: Long
)

@Entity(tableName = "vaccine_records")
data class VaccineRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val code: String,
    val name: String,
    val ageGroupLabel: String,
    val dueOffsetDays: Int,
    val protectionAgainst: String,
    val isCompleted: Boolean = false,
    val completedEpochDay: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "milestone_records")
data class MilestoneRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val monthRange: String,
    val title: String,
    val isAchieved: Boolean = false,
    val achievedEpochDay: Long? = null,
    val memoryNote: String = ""
)
