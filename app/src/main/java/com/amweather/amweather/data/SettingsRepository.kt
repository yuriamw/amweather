package com.amweather.amweather.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository private constructor(private val context: Context) {

    private val gson = Gson()

    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }

        val KEY_LOCATIONS = stringPreferencesKey("locations")
        val KEY_DEFAULT_LOCATION_ID = stringPreferencesKey("default_location_id")
        val KEY_USE_GPS = booleanPreferencesKey("use_gps")
    }

    val locationsFlow: Flow<List<Location>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_LOCATIONS] ?: return@map emptyList()
        val type = object : TypeToken<List<Location>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    val defaultLocationIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_LOCATION_ID]
    }

    val useGpsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_USE_GPS] ?: false
    }

    suspend fun addLocation(location: Location) {
        context.dataStore.edit { prefs ->
            val current = getLocationsFromPrefs(prefs[KEY_LOCATIONS])
            if (current.none { it.id == location.id }) {
                prefs[KEY_LOCATIONS] = gson.toJson(current + location)
            }
        }
    }

    suspend fun removeLocation(locationId: String) {
        context.dataStore.edit { prefs ->
            val current = getLocationsFromPrefs(prefs[KEY_LOCATIONS])
            prefs[KEY_LOCATIONS] = gson.toJson(current.filter { it.id != locationId })
            if (prefs[KEY_DEFAULT_LOCATION_ID] == locationId) {
                prefs.remove(KEY_DEFAULT_LOCATION_ID)
            }
        }
    }

    suspend fun setDefaultLocationId(locationId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_LOCATION_ID] = locationId
            prefs[KEY_USE_GPS] = false
        }
    }

    suspend fun setUseGps(useGps: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USE_GPS] = useGps
        }
    }

    private fun getLocationsFromPrefs(json: String?): List<Location> {
        if (json == null) return emptyList()
        val type = object : TypeToken<List<Location>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
