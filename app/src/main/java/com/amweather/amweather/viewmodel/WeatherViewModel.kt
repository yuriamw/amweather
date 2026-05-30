package com.amweather.amweather.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amweather.amweather.data.Location
import com.amweather.amweather.data.SettingsRepository
import com.amweather.amweather.data.WeatherRepository
import com.amweather.amweather.data.WeatherResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data object NoLocations : WeatherUiState()
    data class Success(val data: WeatherResponse, val updatedAt: String, val location: Location) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(app: Application) : AndroidViewModel(app) {

    private val weatherRepo = WeatherRepository()
    private val settingsRepo = SettingsRepository.get(app)

    private val _selectedLocation = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = _selectedLocation

    val locations: StateFlow<List<Location>> = settingsRepo.locationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pressureUnit: StateFlow<String> = settingsRepo.pressureUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "mbar")

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    init {
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
                    fetchWeather(default)
                }
            }
        }
    }

    fun selectLocation(location: Location) {
        _selectedLocation.value = location
        fetchWeather(location)
    }

    fun fetchWeather(location: Location? = _selectedLocation.value) {
        val loc = location ?: return
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            weatherRepo.fetchWeather(loc.latitude, loc.longitude).fold(
                onSuccess = { data ->
                    val time = LocalTime.now()
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                    _uiState.value = WeatherUiState.Success(data, time, loc)
                },
                onFailure = { err ->
                    _uiState.value = WeatherUiState.Error(err.message ?: "Unknown error")
                }
            )
        }
    }
}
