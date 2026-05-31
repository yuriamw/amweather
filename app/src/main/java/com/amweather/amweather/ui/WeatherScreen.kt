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

package com.amweather.amweather.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amweather.amweather.data.Location
import com.amweather.amweather.data.weatherCodeToDescription
import com.amweather.amweather.viewmodel.WeatherUiState
import com.amweather.amweather.viewmodel.WeatherViewModel
import com.amweather.amweather.data.windDirectionToText
import com.amweather.amweather.data.convertPressure
import com.amweather.amweather.ui.WeatherIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    vm: WeatherViewModel,
    onOpenSettings: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val locations by vm.locations.collectAsStateWithLifecycle()
    val selectedLocation by vm.selectedLocation.collectAsStateWithLifecycle()
    val isRefreshing = state is WeatherUiState.Loading
    var showLocationMenu by remember { mutableStateOf(false) }
    val pressureUnit by vm.pressureUnit.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (locations.size > 1) {
                        Box {
                            TextButton(onClick = { showLocationMenu = true }) {
                                Text(
                                    selectedLocation?.name ?: "Weather",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    " ▾",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = showLocationMenu,
                                onDismissRequest = { showLocationMenu = false }
                            ) {
                                locations.forEach { loc ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(loc.name)
                                                if (loc.id == selectedLocation?.id) {
                                                    Text(
                                                        "  ✓",
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            vm.selectLocation(loc)
                                            showLocationMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(selectedLocation?.name ?: "Weather")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
    ) { padding ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { vm.fetchWeather() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                when (val s = state) {
                    is WeatherUiState.Loading -> CircularProgressIndicator()

                    is WeatherUiState.NoLocations -> LaunchedEffect(Unit) {
                        onOpenSettings()
                    }
                    is WeatherUiState.Error -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { vm.fetchWeather() }) { Text("Retry") }
                    }

                    is WeatherUiState.Success -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val w = s.data.current
                        val u = s.data.current_units

                        WeatherIcon(
                            code = w.weather_code,
                            size = 120.dp
                        )
                        Spacer(Modifier.height(8.dp))

                        // temperature
                        Text(
                            "${w.temperature_2m}${u.temperature_2m}",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            "Feels like ${w.apparent_temperature}${u.apparent_temperature}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(4.dp))
                        Text(weatherCodeToDescription(w.weather_code))
                        Spacer(Modifier.height(8.dp))

                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        // details grid
                        WeatherDetailRow(
                            label = "Humidity",
                            value = "${w.relative_humidity_2m}${u.relative_humidity_2m}"
                        )
                        WeatherDetailRow(
                            label = "Wind",
                            value = "${windDirectionToText(w.wind_direction_10m)} ${w.wind_speed_10m} ${u.wind_speed_10m}"
                        )
                        WeatherDetailRow(
                            label = "Pressure",
                            value = convertPressure(w.surface_pressure, pressureUnit)
                        )

                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Updated: ${s.updatedAt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}
