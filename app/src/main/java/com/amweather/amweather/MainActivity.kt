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

package com.amweather.amweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amweather.amweather.data.SettingsRepository
import com.amweather.amweather.ui.AboutScreen
import com.amweather.amweather.ui.CitySearchScreen
import com.amweather.amweather.ui.IconPreviewScreen
import com.amweather.amweather.ui.SettingsScreen
import com.amweather.amweather.ui.WeatherScreen
import com.amweather.amweather.ui.theme.AmweatherTheme
import com.amweather.amweather.viewmodel.SettingsViewModel
import com.amweather.amweather.viewmodel.WeatherViewModel
import com.amweather.amweather.worker.WeatherWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


object Routes {
    const val WEATHER = "weather"
    const val SETTINGS = "settings"
    const val CITY_SEARCH = "city_search"
    const val ABOUT = "about"
    const val ICON_PREVIEW = "icon_preview"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // start periodic background fetch with saved interval
        lifecycleScope.launch {
            val repo = SettingsRepository.get(applicationContext)
            val value = repo.refreshIntervalValueFlow.first()
            val unit = repo.refreshIntervalUnitFlow.first()
            WeatherWorker.schedule(applicationContext, value, unit)
        }

        setContent {
            AmweatherTheme {
                val navController = rememberNavController()
                val weatherVm: WeatherViewModel = viewModel()
                val settingsVm: SettingsViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = Routes.WEATHER
                ) {
                    composable(Routes.ABOUT) {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.ICON_PREVIEW) {
                        IconPreviewScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Routes.WEATHER) {
                        WeatherScreen(
                            vm = weatherVm,
                            onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                        )
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            vm = settingsVm,
                            onBack = { navController.popBackStack() },
                            onAddCity = { navController.navigate(Routes.CITY_SEARCH) },
                            onIconPreview = { navController.navigate(Routes.ICON_PREVIEW) },
                            onAbout = { navController.navigate(Routes.ABOUT) }
                        )
                    }
                    composable(Routes.CITY_SEARCH) {
                        CitySearchScreen(
                            vm = settingsVm,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
