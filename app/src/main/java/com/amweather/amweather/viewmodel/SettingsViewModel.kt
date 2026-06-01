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
import com.amweather.amweather.data.GeocodingApi
import com.amweather.amweather.data.GeocodingResult
import com.amweather.amweather.data.Location
import com.amweather.amweather.data.SettingsRepository
import com.amweather.amweather.data.WeatherSource
import com.amweather.amweather.worker.WeatherWorker
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SearchState {
    data object Idle : SearchState()
    data object Searching : SearchState()
    data class Results(val items: List<GeocodingResult>) : SearchState()
    data class Error(val message: String) : SearchState()
}

@OptIn(FlowPreview::class)
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    val repo = SettingsRepository.get(app)

    val locations: StateFlow<List<Location>> = repo.locationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultLocationId: StateFlow<String?> = repo.defaultLocationIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val useGps: StateFlow<Boolean> = repo.useGpsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pressureUnit: StateFlow<String> = repo.pressureUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "mbar")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState

    val refreshIntervalValue: StateFlow<Int> = repo.refreshIntervalValueFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val refreshIntervalUnit: StateFlow<String> = repo.refreshIntervalUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "hours")

    val weatherSource: StateFlow<WeatherSource> = repo.weatherSourceFlow
        .map { runCatching { WeatherSource.valueOf(it) }.getOrDefault(WeatherSource.OPEN_METEO) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeatherSource.OPEN_METEO)

    val windUnit: StateFlow<String> = repo.windUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ms")

    init {
        // debounce search — wait 500ms after user stops typing
        _searchQuery
            .debounce(500)
            .filter { it.length >= 2 }
            .onEach { query ->
                _searchState.value = SearchState.Searching
                runCatching {
                    GeocodingApi.service.searchCity(query)
                }.fold(
                    onSuccess = { response ->
                        val results = response.results ?: emptyList()
                        _searchState.value = if (results.isEmpty())
                            SearchState.Error("No cities found")
                        else
                            SearchState.Results(results)
                    },
                    onFailure = {
                        _searchState.value = SearchState.Error(it.message ?: "Search failed")
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.length < 2) _searchState.value = SearchState.Idle
    }

    fun addLocation(result: GeocodingResult) {
        viewModelScope.launch {
            val location = result.toLocation()
            repo.addLocation(location)
            // auto-set as default if it's the first one
            if (locations.value.isEmpty()) {
                repo.setDefaultLocationId(location.id)
            }
        }
    }

    fun removeLocation(locationId: String) {
        viewModelScope.launch { repo.removeLocation(locationId) }
    }

    fun setDefault(locationId: String) {
        viewModelScope.launch { repo.setDefaultLocationId(locationId) }
    }

    fun setUseGps(value: Boolean) {
        viewModelScope.launch { repo.setUseGps(value) }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchState.value = SearchState.Idle
    }

    fun setPressureUnit(unit: String) {
        viewModelScope.launch { repo.setPressureUnit(unit) }
    }

    fun setRefreshInterval(value: Int, unit: String) {
        viewModelScope.launch {
            repo.setRefreshInterval(value, unit)
            WeatherWorker.schedule(getApplication(), value, unit)
        }
    }

    fun setWeatherSource(source: WeatherSource) {
        viewModelScope.launch { repo.setWeatherSource(source) }
    }

    fun setWindUnit(unit: String) {
        viewModelScope.launch { repo.setWindUnit(unit) }
    }
}
