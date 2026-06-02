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


package com.amweather.amweather.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

// Open-Meteo API response structure
data class WeatherResponse(
    val current: CurrentWeather,
    val current_units: CurrentUnits
)

data class CurrentWeather(
    val temperature_2m: Double,
    val apparent_temperature: Double,
    val relative_humidity_2m: Int,
    val wind_speed_10m: Double,
    val wind_direction_10m: Int,
    val surface_pressure: Double,
    val weather_code: Int
)

data class CurrentUnits(
    val temperature_2m: String,
    val apparent_temperature: String,
    val relative_humidity_2m: String,
    val wind_speed_10m: String,
    val wind_direction_10m: String,
    val surface_pressure: String
)

data class DailyWeatherResponse(
    val daily: DailyWeather
)

data class DailyWeather(
    val sunrise: List<String>,
    val sunset: List<String>,
    val moon_phase: List<Double>?
)

data class SunMoonData(
    val sunrise: String,
    val sunset: String,
    val moonrise: String?,
    val moonset: String?,
    val moonPhaseDegrees: Double?,
    val moonPhaseDescription: String?,
    val moonPhaseEmoji: String?
)


data class OpenMeteoForecastResponse(
    val hourly: OpenMeteoHourly,
    val daily: OpenMeteoDaily
)

data class OpenMeteoHourly(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weather_code: List<Int>
)

data class OpenMeteoDaily(
    val time: List<String>,
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)

fun WeatherResponse.toWeatherData() = WeatherData(
    temperature = current.temperature_2m,
    apparentTemperature = current.apparent_temperature,
    humidity = current.relative_humidity_2m,
    windSpeed = current.wind_speed_10m,
    windDirection = current.wind_direction_10m,
    pressure = current.surface_pressure,
    weatherCode = current.weather_code,
    source = WeatherSource.OPEN_METEO
)

fun weatherCodeToDescription(code: Int): String = when (code) {
    0 -> "Clear sky"
    1 -> "Mainly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Foggy"
    51, 53, 55 -> "Drizzle"
    61, 63, 65 -> "Rain"
    71, 73, 75 -> "Snow"
    77 -> "Snow grains"
    80, 81, 82 -> "Rain showers"
    85, 86 -> "Snow showers"
    95 -> "Thunderstorm"
    96, 99 -> "Thunderstorm with hail"
    else -> "Unknown"
}

fun windDirectionToText(degrees: Int): String {
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = ((degrees + 22.5) / 45).toInt() % 8
    return directions[index]
}

fun convertPressure(mbar: Double, unit: String): String = when (unit) {
    "mmhg" -> "${"%.0f".format(mbar * 0.750062)} mmHg"
    else -> "$mbar mbar"
}

fun isDaytime(): Boolean {
    val hour = LocalTime.now().hour
    return hour in 6..21
}

fun convertWind(ms: Double, unit: String): String = when (unit) {
    "kmh" -> "${"%.1f".format(ms * 3.6)} km/h"
    else -> "${"%.1f".format(ms)} m/s"
}

fun parseTimeFromIso(iso: String?): String? {
    if (iso == null) return null
    return runCatching { iso.substringAfter("T").substring(0, 5) }.getOrNull()
}

fun SunMoonData.withMoonData(
    moonrise: String?,
    moonset: String?,
    moonPhase: Double?
): SunMoonData = copy(
    moonrise = moonrise,
    moonset = moonset,
    moonPhaseDegrees = moonPhase,
    moonPhaseDescription = moonPhase?.let { moonDegreesToDescription(it) },
    moonPhaseEmoji = moonPhase?.let { moonDegreesToEmoji(it) }
)

fun moonDegreesToDescription(degrees: Double): String = when {
    degrees < 10 || degrees > 350 -> "New Moon"
    degrees < 80 -> "Waxing Crescent"
    degrees < 100 -> "First Quarter"
    degrees < 170 -> "Waxing Gibbous"
    degrees < 190 -> "Full Moon"
    degrees < 260 -> "Waning Gibbous"
    degrees < 280 -> "Last Quarter"
    else -> "Waning Crescent"
}

fun moonDegreesToEmoji(degrees: Double): String = when {
    degrees < 10 || degrees > 350 -> "🌑"
    degrees < 80 -> "🌒"
    degrees < 100 -> "🌓"
    degrees < 170 -> "🌔"
    degrees < 190 -> "🌕"
    degrees < 260 -> "🌖"
    degrees < 280 -> "🌗"
    else -> "🌘"
}

fun DailyWeatherResponse.toSunMoonData(): SunMoonData? {
    val d = daily
    if (d.sunrise.isEmpty()) return null
    val moonPhase = d.moon_phase?.firstOrNull()?.times(360.0)
    return SunMoonData(
        sunrise = parseTimeFromIso(d.sunrise.firstOrNull()) ?: return null,
        sunset = parseTimeFromIso(d.sunset.firstOrNull()) ?: return null,
        moonrise = null,
        moonset = null,
        moonPhaseDegrees = moonPhase,
        moonPhaseDescription = moonPhase?.let { moonDegreesToDescription(it) },
        moonPhaseEmoji = moonPhase?.let { moonDegreesToEmoji(it) }
    )
}

fun OpenMeteoForecastResponse.toForecastData(): ForecastData {
    val now = LocalDateTime.now()
    val today = LocalDate.now()

    // parse hourly — keep 12 past + 48 future
    val hourlyList = hourly.time.mapIndexedNotNull { i, timeStr ->
        runCatching {
            val dt = LocalDateTime.parse(timeStr)
            val hoursFromNow = java.time.Duration.between(now, dt).toHours()
            if (hoursFromNow < -12 || hoursFromNow > 48) return@mapIndexedNotNull null
            HourlyForecast(
                time = dt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                date = dt.toLocalDate().toString(),
                temperature = hourly.temperature_2m[i],
                weatherCode = hourly.weather_code[i],
                isCurrent = hoursFromNow == 0L ||
                        (hoursFromNow == -1L && now.minute > 0)
            )
        }.getOrNull()
    }

    // mark actual current hour
    val currentIndex = hourlyList.indexOfFirst {
        val dt = LocalDateTime.parse("${it.date}T${it.time}")
        val h = java.time.Duration.between(now, dt).toHours()
        h in -1..0
    }.coerceAtLeast(0)
    val hourly = hourlyList.mapIndexed { i, h ->
        h.copy(isCurrent = i == currentIndex)
    }

    // parse daily — keep 3 past + 7 future
    val dailyList = daily.time.mapIndexedNotNull { i, dateStr ->
        runCatching {
            val date = LocalDate.parse(dateStr)
            val daysFromNow = java.time.temporal.ChronoUnit.DAYS.between(today, date)
            if (daysFromNow < -3 || daysFromNow > 7) return@mapIndexedNotNull null
            DailyForecast(
                date = dateStr,
                dayOfWeek = if (date == today) "Today"
                else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                tempDay = daily.temperature_2m_max[i],
                tempNight = daily.temperature_2m_min[i],
                weatherCode = daily.weather_code[i],
                isCurrent = date == today
            )
        }.getOrNull()
    }

    return ForecastData(hourly = hourly, daily = dailyList)
}
