package com.littlecare.babytracker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "baby_profile_prefs")

data class BabyProfile(
    val isSetupComplete: Boolean = false,
    val name: String = "",
    val birthDateEpochDay: Long = 0L,
    val birthTimeText: String = "08:00 AM"
)

class BabyPreferences(private val context: Context) {
    private object Keys {
        val IS_SETUP_COMPLETE = booleanPreferencesKey("is_setup_complete")
        val NAME = stringPreferencesKey("baby_name")
        val BIRTH_DATE_EPOCH_DAY = longPreferencesKey("birth_date_epoch_day")
        val BIRTH_TIME_TEXT = stringPreferencesKey("birth_time_text")
    }

    val babyProfileFlow: Flow<BabyProfile> = context.dataStore.data.map { prefs ->
        BabyProfile(
            isSetupComplete = prefs[Keys.IS_SETUP_COMPLETE] ?: false,
            name = prefs[Keys.NAME] ?: "",
            birthDateEpochDay = prefs[Keys.BIRTH_DATE_EPOCH_DAY] ?: 0L,
            birthTimeText = prefs[Keys.BIRTH_TIME_TEXT] ?: "08:00 AM"
        )
    }

    suspend fun saveBabyProfile(name: String, birthDateEpochDay: Long, birthTimeText: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_SETUP_COMPLETE] = true
            prefs[Keys.NAME] = name.trim()
            prefs[Keys.BIRTH_DATE_EPOCH_DAY] = birthDateEpochDay
            prefs[Keys.BIRTH_TIME_TEXT] = birthTimeText
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
