package com.amweather.amweather.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class IconEntry(val code: Int, val label: String)

private val allIcons = listOf(
    IconEntry(0,  "Clear day"),
    IconEntry(-1, "Clear night"),   // -1 = force night
    IconEntry(2,  "Partly cloudy day"),
    IconEntry(-2, "Partly cloudy night"),
    IconEntry(3,  "Overcast day"),
    IconEntry(-3, "Overcast night"),
    IconEntry(45, "Fog"),
    IconEntry(51, "Drizzle"),
    IconEntry(61, "Rain"),
    IconEntry(65, "Heavy rain"),
    IconEntry(71, "Snow"),
    IconEntry(95, "Thunderstorm"),
    IconEntry(96, "Thunder + rain"),
    IconEntry(-4, "Wind"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPreviewScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Icon Preview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allIcons) { entry ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                ) {
                    // negative codes force night variants directly
                    when (entry.code) {
                        -1 -> MoonIcon(size = 72.dp)
                        -2 -> PartlyCloudyNightIcon(size = 72.dp)
                        -3 -> OvercastNightIcon(size = 72.dp)
                        -4 -> WindIcon(size = 72.dp)
                        else -> WeatherIcon(code = entry.code, size = 72.dp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        entry.label,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
