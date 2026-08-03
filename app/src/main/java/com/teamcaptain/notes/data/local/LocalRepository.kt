package com.teamcaptain.notes.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teamcaptain.notes.data.model.AppData
import com.teamcaptain.notes.data.model.MatchScheduleCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** DataStore Preferences instance scoped to the application context. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "teamcaptain_notes")

/**
 * Single source of truth for all locally stored user data.
 *
 * The entire [AppData] tree is serialized to one JSON string under [APP_DATA_KEY].
 * Reads are defensive: any missing store, missing field or corrupted JSON falls
 * back to a valid default [AppData] instead of throwing.
 */
class LocalRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true   // tolerate forward-incompatible fields
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true   // null/invalid enum -> default value
    }

    /** Stream of the full app data, always emitting a valid object. */
    val appData: Flow<AppData> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val raw = prefs[APP_DATA_KEY]
            decode(raw)
        }

    private fun decode(raw: String?): AppData {
        if (raw.isNullOrBlank()) return AppData()
        return runCatching { json.decodeFromString<AppData>(raw) }.getOrElse { AppData() }
    }

    /** Atomically read-modify-write the whole app data. */
    suspend fun update(transform: (AppData) -> AppData) {
        context.dataStore.edit { prefs ->
            val existing = decode(prefs[APP_DATA_KEY])
            val updated = runCatching { transform(existing) }.getOrElse { existing }
            prefs[APP_DATA_KEY] = runCatching { json.encodeToString(updated) }
                .getOrElse { json.encodeToString(existing) }
        }
    }

    /** Overwrite everything with a fresh default (Reset all local data). */
    suspend fun resetAll() {
        context.dataStore.edit { prefs ->
            prefs[APP_DATA_KEY] = json.encodeToString(AppData())
        }
    }

    suspend fun clearMatchCache() = update {
        it.copy(matchScheduleCache = MatchScheduleCache())
    }

    companion object {
        private val APP_DATA_KEY = stringPreferencesKey("app_data_json")
    }
}
