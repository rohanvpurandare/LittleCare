package com.littlecare.babytracker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "littlecare_prefs")

class UserPrefs(private val context: Context) {
    companion object {
        val ACTIVE_BABY_ID = longPreferencesKey("active_baby_id")
        val NIGHT_VISION_MODE = booleanPreferencesKey("night_vision_mode")
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
    }

    val activeBabyId: Flow<Long> = context.dataStore.data.map { it[ACTIVE_BABY_ID] ?: -1L }
    val nightVisionMode: Flow<Boolean> = context.dataStore.data.map { it[NIGHT_VISION_MODE] ?: false }
    val biometricLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[BIOMETRIC_LOCK_ENABLED] ?: false }

    suspend fun setActiveBabyId(id: Long) {
        context.dataStore.edit { it[ACTIVE_BABY_ID] = id }
    }

    suspend fun setNightVisionMode(enabled: Boolean) {
        context.dataStore.edit { it[NIGHT_VISION_MODE] = enabled }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        context.dataStore.edit { it[BIOMETRIC_LOCK_ENABLED] = enabled }
    }
}
