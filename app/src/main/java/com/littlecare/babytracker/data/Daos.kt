package com.littlecare.babytracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestampMillis DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs ORDER BY timestampMillis DESC")
    suspend fun getAllLogsSnapshot(): List<ActivityLog>

    @Query("SELECT * FROM activity_logs WHERE timestampMillis >= :startOfDayMillis ORDER BY timestampMillis DESC")
    fun getLogsSince(startOfDayMillis: Long): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog): Long

    @Delete
    suspend fun deleteLog(log: ActivityLog)
}

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growth_entries ORDER BY weekNumber ASC, recordedDateEpochDay ASC")
    fun getAllGrowthEntries(): Flow<List<GrowthEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthEntry(entry: GrowthEntry)

    @Delete
    suspend fun deleteGrowthEntry(entry: GrowthEntry)
}

@Dao
interface VaccineDao {
    @Query("SELECT * FROM vaccine_records ORDER BY daysAfterBirth ASC, name ASC")
    fun getAllVaccines(): Flow<List<VaccineRecord>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vaccines: List<VaccineRecord>)

    @Query(
        """
        UPDATE vaccine_records 
        SET isAdministered = :isAdministered, administeredDateEpochDay = :administeredDateEpochDay 
        WHERE code = :code
        """
    )
    suspend fun updateVaccineStatus(
        code: String,
        isAdministered: Boolean,
        administeredDateEpochDay: Long?
    )
}
