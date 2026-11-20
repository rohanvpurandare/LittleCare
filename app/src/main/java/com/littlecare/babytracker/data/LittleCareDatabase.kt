package com.littlecare.babytracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BabyProfile::class,
        ActivityLog::class,
        GrowthEntry::class,
        VaccineRecord::class,
        MilestoneRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LittleCareDatabase : RoomDatabase() {
    abstract fun babyDao(): BabyDao

    companion object {
        @Volatile
        private var INSTANCE: LittleCareDatabase? = null

        fun getDatabase(context: Context): LittleCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LittleCareDatabase::class.java,
                    "littlecare_offline_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
