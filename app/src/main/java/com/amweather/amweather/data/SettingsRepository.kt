/*
 * Copyright (C) 2026 yuriamw (https://github.com/yuriamw)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
        val KEY_PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
        val KEY_REFRESH_INTERVAL_VALUE = androidx.datastore.preferences.core.intPreferencesKey("refresh_interval_value")
        val KEY_REFRESH_INTERVAL_UNIT = stringPreferencesKey("refresh_interval_unit")    }
        val KEY_WEATHER_SOURCE = stringPreferencesKey("weather_source")

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

    val pressureUnitFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PRESSURE_UNIT] ?: "mbar"
    }

    val refreshIntervalValueFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_REFRESH_INTERVAL_VALUE] ?: 1
    }

    val refreshIntervalUnitFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_REFRESH_INTERVAL_UNIT] ?: "hours"
    }

    val weatherSourceFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_WEATHER_SOURCE] ?: WeatherSource.OPEN_METEO.name
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

    suspend fun setPressureUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PRESSURE_UNIT] = unit
        }
    }

    suspend fun setRefreshInterval(value: Int, unit: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REFRESH_INTERVAL_VALUE] = value
            prefs[KEY_REFRESH_INTERVAL_UNIT] = unit
        }
    }

    private fun getLocationsFromPrefs(json: String?): List<Location> {
        if (json == null) return emptyList()
        val type = object : TypeToken<List<Location>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    suspend fun setWeatherSource(source: WeatherSource) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WEATHER_SOURCE] = source.name
        }
    }
}
