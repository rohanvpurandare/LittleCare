package com.littlecare.babytracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyDao {
    // Profiles
    @Query("SELECT * FROM baby_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<BabyProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BabyProfile): Long

    @Update
    suspend fun updateProfile(profile: BabyProfile)

    // Activity Logs
    @Query("SELECT * FROM activity_logs WHERE babyId = :babyId ORDER BY timestampMillis DESC")
    fun getActivityLogs(babyId: Long): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(log: ActivityLog): Long

    @Delete
    suspend fun deleteActivity(log: ActivityLog)

    // Growth
    @Query("SELECT * FROM growth_entries WHERE babyId = :babyId ORDER BY weekNumber ASC")
    fun getGrowthEntries(babyId: Long): Flow<List<GrowthEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowth(entry: GrowthEntry): Long

    @Delete
    suspend fun deleteGrowth(entry: GrowthEntry)

    // Vaccines
    @Query("SELECT * FROM vaccine_records WHERE babyId = :babyId ORDER BY dueOffsetDays ASC, id ASC")
    fun getVaccines(babyId: Long): Flow<List<VaccineRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccines(vaccines: List<VaccineRecord>)

    @Update
    suspend fun updateVaccine(vaccine: VaccineRecord)

    // Milestones
    @Query("SELECT * FROM milestone_records WHERE babyId = :babyId ORDER BY id ASC")
    fun getMilestones(babyId: Long): Flow<List<MilestoneRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneRecord>)

    @Update
    suspend fun updateMilestone(milestone: MilestoneRecord)
}
