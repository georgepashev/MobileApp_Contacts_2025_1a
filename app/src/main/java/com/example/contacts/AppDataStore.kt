package com.example.contacts

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "sync_prefs")
class AppDataStore(private val context: Context) {
    private val KEY_LAST_SYNC = longPreferencesKey("lastSyncAt")

    suspend fun getLong(key: String, default: Long = 0L): Long {
        return when (key) {
            "lastSyncAt" -> context.dataStore.data.first()[KEY_LAST_SYNC] ?: default
            else -> default
        }
    }
    suspend fun putLong(key: String, value: Long) {
        when (key) {
            "lastSyncAt" -> context.dataStore.edit { it[KEY_LAST_SYNC] = value }
        }
    }
}