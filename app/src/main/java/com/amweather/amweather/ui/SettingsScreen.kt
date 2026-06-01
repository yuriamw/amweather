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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amweather.amweather.data.Location
import com.amweather.amweather.data.WeatherSource
import com.amweather.amweather.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onBack: () -> Unit,
    onAddCity: () -> Unit,
    onIconPreview: () -> Unit,
    onAbout: () -> Unit
) {
    val locations by vm.locations.collectAsStateWithLifecycle()
    val defaultId by vm.defaultLocationId.collectAsStateWithLifecycle()
    val pressureUnit by vm.pressureUnit.collectAsStateWithLifecycle()
    val refreshValue by vm.refreshIntervalValue.collectAsStateWithLifecycle()
    val refreshUnit by vm.refreshIntervalUnit.collectAsStateWithLifecycle()
    val weatherSource by vm.weatherSource.collectAsStateWithLifecycle()
    val windUnit by vm.windUnit.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddCity) {
                        Icon(Icons.Default.Add, contentDescription = "Add city")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    "Saved Locations",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (locations.isEmpty()) {
                item {
                    Text(
                        "No locations saved yet. Tap + to add a city.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(locations, key = { it.id }) { location ->
                LocationItem(
                    location = location,
                    isDefault = location.id == defaultId,
                    onSetDefault = { vm.setDefault(location.id) },
                    onDelete = { vm.removeLocation(location.id) }
                )
                HorizontalDivider()
            }
            item {
                HorizontalDivider()
                Text(
                    "Units",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pressure", modifier = Modifier.weight(1f))
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = pressureUnit == "mbar",
                            onClick = { vm.setPressureUnit("mbar") },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) { Text("mbar") }
                        SegmentedButton(
                            selected = pressureUnit == "mmhg",
                            onClick = { vm.setPressureUnit("mmhg") },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) { Text("mmHg") }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wind speed", modifier = Modifier.weight(1f))
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = windUnit == "ms",
                            onClick = { vm.setWindUnit("ms") },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) { Text("m/s") }
                        SegmentedButton(
                            selected = windUnit == "kmh",
                            onClick = { vm.setWindUnit("kmh") },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) { Text("km/h") }
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                Text(
                    "Weather Source",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                var expanded by remember { mutableStateOf(false) }
                val sources = listOf(
                    WeatherSource.OPEN_METEO to "Open-Meteo",
                    WeatherSource.MET_NORWAY to "MET Norway"
                )
                val selectedLabel = sources.first { it.first == weatherSource }.second

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Source", modifier = Modifier.weight(1f))
                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text(selectedLabel)
                            Spacer(Modifier.width(4.dp))
                            Text("▾")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            sources.forEach { (source, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(label)
                                            if (source == weatherSource) {
                                                Text(
                                                    "  ✓",
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        vm.setWeatherSource(source)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            item {
                Text(
                    "Refresh Interval",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }


            item {
                val min = if (refreshUnit == "minutes") 15 else 1
                val max = if (refreshUnit == "minutes") 59 else 24

                // local draft so user can type freely
                var draft by remember(refreshValue) { mutableStateOf(refreshValue.toString()) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (refreshValue > min) {
                                vm.setRefreshInterval(refreshValue - 1, refreshUnit)
                                draft = (refreshValue - 1).toString()
                            }
                        },
                        enabled = refreshValue > min
                    ) { Text("−", style = MaterialTheme.typography.titleLarge) }

                    OutlinedTextField(
                        value = draft,
                        onValueChange = { input ->
                            draft = input.filter { it.isDigit() }.take(2)
                            val num = draft.toIntOrNull()
                            if (num != null && num in min..max) {
                                vm.setRefreshInterval(num, refreshUnit)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.width(72.dp),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )

                    IconButton(
                        onClick = {
                            if (refreshValue < max) {
                                vm.setRefreshInterval(refreshValue + 1, refreshUnit)
                                draft = (refreshValue + 1).toString()
                            }
                        },
                        enabled = refreshValue < max
                    ) { Text("+", style = MaterialTheme.typography.titleLarge) }

                    Spacer(Modifier.weight(1f))

                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = refreshUnit == "minutes",
                            onClick = { if (refreshUnit != "minutes") vm.setRefreshInterval(15, "minutes") },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) { Text("min") }
                        SegmentedButton(
                            selected = refreshUnit == "hours",
                            onClick = { if (refreshUnit != "hours") vm.setRefreshInterval(1, "hours") },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) { Text("hrs") }
                    }
                }
            }

            item {
                HorizontalDivider()
            }
            item {
                TextButton(
                    onClick = onIconPreview,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text("Dev: Icon Preview")
                }
            }
            item {
                TextButton(
                    onClick = onAbout,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text("About")
                }
            }
        }
    }
}

@Composable
private fun LocationItem(
    location: Location,
    isDefault: Boolean,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(location.name, style = MaterialTheme.typography.bodyLarge)
            Text(location.country, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isDefault) {
            AssistChip(
                onClick = {},
                label = { Text("Default") },
                modifier = Modifier.padding(end = 8.dp)
            )
        } else {
            TextButton(onClick = onSetDefault) { Text("Set default") }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error)
        }
    }
}
