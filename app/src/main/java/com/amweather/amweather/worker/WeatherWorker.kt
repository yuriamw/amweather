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

package com.amweather.amweather.worker

import android.content.Context
import androidx.work.*
import com.amweather.amweather.data.SettingsRepository
import com.amweather.amweather.data.WeatherCache
import com.amweather.amweather.data.WeatherRepository
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class WeatherWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settingsRepo = SettingsRepository.get(applicationContext)
        val weatherRepo = WeatherRepository()
        val cache = WeatherCache.get(applicationContext)

        val locations = settingsRepo.locationsFlow.first()
        if (locations.isEmpty()) return Result.success()

        var anyFailed = false
        val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

        for (location in locations) {
            weatherRepo.fetchWeather(location.latitude, location.longitude).fold(
                onSuccess = { response ->
                    cache.store(location.id, response, time)
                },
                onFailure = {
                    anyFailed = true
                }
            )
        }

        return if (anyFailed) Result.retry() else Result.success()
    }

    companion object {
        const val WORK_NAME = "weather_periodic_fetch"

        fun schedule(context: Context, intervalValue: Int, intervalUnit: String) {
            val minutes = when (intervalUnit) {
                "hours" -> intervalValue * 60L
                else -> intervalValue.toLong()
            }.coerceAtLeast(15L) // WorkManager minimum is 15 minutes

            val request = PeriodicWorkRequestBuilder<WeatherWorker>(
                minutes, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
