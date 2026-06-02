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

package com.amweather.amweather.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amweather.amweather.data.ForecastData
import com.amweather.amweather.data.Location
import com.amweather.amweather.data.SettingsRepository
import com.amweather.amweather.data.SunMoonData
import com.amweather.amweather.data.WeatherCache
import com.amweather.amweather.data.WeatherData
import com.amweather.amweather.data.WeatherRepository
import com.amweather.amweather.data.WeatherSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data object NoLocations : WeatherUiState()

    data class Success(
        val data: WeatherData,
        val sunMoon: SunMoonData?,
        val forecast: ForecastData?,
        val updatedAt: String,
        val location: Location,
        val fetchError: String? = null,
        val sunMoonError: String? = null
    ) : WeatherUiState()

    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(app: Application) : AndroidViewModel(app) {

    private val weatherRepo = WeatherRepository()
    private val settingsRepo = SettingsRepository.get(app)
    private val cache = WeatherCache.get(app)

    private val _selectedLocation = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = _selectedLocation

    val locations: StateFlow<List<Location>> = settingsRepo.locationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pressureUnit: StateFlow<String> = settingsRepo.pressureUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "mbar")

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    val weatherSource: StateFlow<WeatherSource> = settingsRepo.weatherSourceFlow
        .map { runCatching { WeatherSource.valueOf(it) }.getOrDefault(WeatherSource.OPEN_METEO) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeatherSource.OPEN_METEO)

    val windUnit: StateFlow<String> = settingsRepo.windUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ms")

    init {
        // re-fetch when weather source changes
        viewModelScope.launch {
            weatherSource.drop(1).collect {
                fetchWeather()
            }
        }

        viewModelScope.launch {
            combine(
                settingsRepo.locationsFlow,
                settingsRepo.defaultLocationIdFlow
            ) { locs, defaultId ->
                Pair(locs, defaultId)
            }.collect { (locs, defaultId) ->
                if (locs.isEmpty()) {
                    _uiState.value = WeatherUiState.NoLocations
                } else if (_selectedLocation.value == null) {
                    val default = locs.find { it.id == defaultId } ?: locs.first()
                    _selectedLocation.value = default
                    loadWeather(default)
                }
            }
        }
    }

    fun selectLocation(location: Location) {
        _selectedLocation.value = location
        loadWeather(location)
    }

    // load from cache first, then fetch fresh
    private fun loadWeather(location: Location) {
        viewModelScope.launch {
            val cached = cache.flowFor(location.id).first()
            if (cached != null) {
                _uiState.value = WeatherUiState.Success(
                    cached.data, cached.sunMoon, cached.forecast,
                    cached.updatedAt, location
                )
            } else {
                _uiState.value = WeatherUiState.Loading
            }
            fetchWeather(location)
        }
    }

    fun dismissFetchError() {
        val s = _uiState.value as? WeatherUiState.Success ?: return
        _uiState.value = s.copy(fetchError = null)
    }

    fun dismissSunMoonError() {
        val s = _uiState.value as? WeatherUiState.Success ?: return
        _uiState.value = s.copy(sunMoonError = null)
    }

    fun fetchWeather(location: Location? = _selectedLocation.value) {
        val loc = location ?: return
        viewModelScope.launch {
            if (_uiState.value !is WeatherUiState.Success) {
                _uiState.value = WeatherUiState.Loading
            }
            val source = weatherSource.value

            val weatherResult = weatherRepo.fetchWeather(loc.latitude, loc.longitude, source)
            val sunMoonResult = weatherRepo.fetchSunMoon(loc.latitude, loc.longitude, source)
            val forecastResult = weatherRepo.fetchForecast(loc.latitude, loc.longitude, source)

            weatherResult.fold(
                onSuccess = { data ->
                    val time = LocalTime.now()
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                    val sunMoon = sunMoonResult.getOrNull()
                    val sunMoonError = sunMoonResult.exceptionOrNull()?.message
                    val forecast = forecastResult.getOrNull()
                    cache.store(loc.id, data, sunMoon, forecast, time)
                    _uiState.value = WeatherUiState.Success(
                        data, sunMoon, forecast, time, loc,
                        sunMoonError = sunMoonError
                    )
                },
                onFailure = { err ->
                    val msg = err.message ?: "Unknown error"
                    val current = _uiState.value
                    if (current is WeatherUiState.Success) {
                        _uiState.value = current.copy(fetchError = msg)
                    } else {
                        _uiState.value = WeatherUiState.Error(msg)
                    }
                }
            )
        }
    }
}