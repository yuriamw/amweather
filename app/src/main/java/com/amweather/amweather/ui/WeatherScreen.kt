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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amweather.amweather.data.DailyForecast
import com.amweather.amweather.data.HourlyForecast
import com.amweather.amweather.data.SunMoonData
import com.amweather.amweather.data.WeatherSource
import com.amweather.amweather.data.convertPressure
import com.amweather.amweather.data.convertWind
import com.amweather.amweather.data.weatherCodeToDescription
import com.amweather.amweather.data.windDirectionToText
import com.amweather.amweather.viewmodel.WeatherUiState
import com.amweather.amweather.viewmodel.WeatherViewModel

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
    val windUnit by vm.windUnit.collectAsStateWithLifecycle()

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
                        val w = s.data

                        s.fetchError?.let { err ->
                            ErrorBanner(
                                message = "Weather error: $err",
                                onDismiss = { vm.dismissFetchError() }
                            )
                        }
                        s.sunMoonError?.let { err ->
                            ErrorBanner(
                                message = "Sun/moon error: $err",
                                onDismiss = { vm.dismissSunMoonError() }
                            )
                        }

                        WeatherIcon(code = w.weatherCode, size = 120.dp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${"%.1f".format(w.temperature)}°C",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            "Feels like ${"%.1f".format(w.apparentTemperature)}°C",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(weatherCodeToDescription(w.weatherCode))
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        WeatherDetailRow("Humidity", "${w.humidity}%")
                        WeatherDetailRow(
                            "Wind",
                            "${windDirectionToText(w.windDirection)} ${convertWind(w.windSpeed, windUnit)}"
                        )
                        w.windGust?.let {
                            WeatherDetailRow("Gusts", convertWind(it, windUnit))
                        }
                        WeatherDetailRow("Pressure", convertPressure(w.pressure, pressureUnit))
                        s.sunMoon?.let { sm ->
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            SunMoonRow(sm)
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Updated: ${s.updatedAt} · ${w.source.displayName()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        s.forecast?.let { forecast ->
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            HourlyForecastRow(forecast.hourly)
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            DailyForecastRow(forecast.daily)
                        }
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

private fun WeatherSource.displayName() = when (this) {
    WeatherSource.OPEN_METEO -> "Open-Meteo"
    WeatherSource.MET_NORWAY -> "MET Norway"
}

@Composable
private fun SunMoonRow(sm: SunMoonData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // sunrise / sunset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("☀ ↑ ${sm.sunrise}", style = MaterialTheme.typography.bodyMedium)
            Text("☀ ↓ ${sm.sunset}", style = MaterialTheme.typography.bodyMedium)
        }
        // moonrise / moonset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("☽ ↑ ${sm.moonrise ?: "--:--"}", style = MaterialTheme.typography.bodyMedium)
            Text("☽ ↓ ${sm.moonset ?: "--:--"}", style = MaterialTheme.typography.bodyMedium)
        }
        // moon phase
        if (sm.moonPhaseEmoji != null && sm.moonPhaseDescription != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "${sm.moonPhaseEmoji} ${sm.moonPhaseDescription}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HourlyForecastRow(hourly: List<HourlyForecast>) {
    val listState = rememberLazyListState()
    val currentIndex = hourly.indexOfFirst { it.isCurrent }.coerceAtLeast(0)

    LaunchedEffect(currentIndex) {
        if (currentIndex > 0) listState.scrollToItem((currentIndex - 2).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(hourly) { item ->
            HourlyItem(item)
        }
    }
}

@Composable
private fun HourlyItem(item: HourlyForecast) {
    Card(
        modifier = Modifier.width(56.dp),
        border = if (item.isCurrent)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else null,
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCurrent)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .fillMaxWidth()
        ) {
            Text(
                item.time,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            WeatherIcon(code = item.weatherCode, size = 32.dp)
            Spacer(Modifier.height(4.dp))
            Text(
                "${"%.0f".format(item.temperature)}°",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun DailyForecastRow(daily: List<DailyForecast>) {
    val listState = rememberLazyListState()
    val currentIndex = daily.indexOfFirst { it.isCurrent }.coerceAtLeast(0)

    LaunchedEffect(currentIndex) {
        if (currentIndex > 0) listState.scrollToItem((currentIndex - 1).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(daily) { item ->
            DailyItem(item)
        }
    }
}

@Composable
private fun DailyItem(item: DailyForecast) {
    Card(
        modifier = Modifier.width(64.dp),
        border = if (item.isCurrent)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else null,
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCurrent)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .fillMaxWidth()
        ) {
            Text(
                item.dayOfWeek,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            WeatherIcon(code = item.weatherCode, size = 36.dp)
            Spacer(Modifier.height(4.dp))
            Text(
                "${"%.0f".format(item.tempDay)}°",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                "${"%.0f".format(item.tempNight)}°",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp, end = 2.dp)
        ) {
            Text(
                message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
