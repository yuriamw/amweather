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
