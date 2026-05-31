package com.amweather.amweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amweather.amweather.ui.CitySearchScreen
import com.amweather.amweather.ui.SettingsScreen
import com.amweather.amweather.ui.WeatherScreen
import com.amweather.amweather.ui.theme.AmweatherTheme
import com.amweather.amweather.viewmodel.SettingsViewModel
import com.amweather.amweather.viewmodel.WeatherViewModel
import androidx.lifecycle.lifecycleScope
import com.amweather.amweather.data.SettingsRepository
import com.amweather.amweather.worker.WeatherWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.amweather.amweather.ui.IconPreviewScreen


object Routes {
    const val WEATHER = "weather"
    const val SETTINGS = "settings"
    const val CITY_SEARCH = "city_search"

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
                            onIconPreview = { navController.navigate(Routes.ICON_PREVIEW) }
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
