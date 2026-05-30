package com.amweather.amweather.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amweather.amweather.data.Location
import com.amweather.amweather.viewmodel.SettingsViewModel
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onBack: () -> Unit,
    onAddCity: () -> Unit
) {
    val locations by vm.locations.collectAsStateWithLifecycle()
    val defaultId by vm.defaultLocationId.collectAsStateWithLifecycle()
    val pressureUnit by vm.pressureUnit.collectAsStateWithLifecycle()

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
