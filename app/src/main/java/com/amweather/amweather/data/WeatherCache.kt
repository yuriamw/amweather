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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.weatherDataStore by preferencesDataStore(name = "weather_cache")

class WeatherCache private constructor(private val context: Context) {

    private val gson = Gson()

    companion object {
        @Volatile
        private var INSTANCE: WeatherCache? = null

        fun get(context: Context): WeatherCache =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: WeatherCache(context.applicationContext).also { INSTANCE = it }
            }
    }

    data class CachedWeather(
        val response: WeatherResponse,
        val updatedAt: String,
        val locationId: String
    )

    private fun keyFor(locationId: String) =
        stringPreferencesKey("weather_$locationId")

    suspend fun store(locationId: String, response: WeatherResponse, updatedAt: String) {
        val cached = CachedWeather(response, updatedAt, locationId)
        context.weatherDataStore.edit { prefs ->
            prefs[keyFor(locationId)] = gson.toJson(cached)
        }
    }

    fun flowFor(locationId: String): Flow<CachedWeather?> =
        context.weatherDataStore.data.map { prefs ->
            val json = prefs[keyFor(locationId)] ?: return@map null
            runCatching { gson.fromJson(json, CachedWeather::class.java) }.getOrNull()
        }

    suspend fun clear(locationId: String) {
        context.weatherDataStore.edit { prefs ->
            prefs.remove(keyFor(locationId))
        }
    }
}
