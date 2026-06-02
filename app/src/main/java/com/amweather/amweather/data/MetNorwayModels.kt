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

data class MetNorwayResponse(
    val properties: MetNorwayProperties
)

data class MetNorwayProperties(
    val timeseries: List<MetNorwayTimeSeries>
)

data class MetNorwayTimeSeries(
    val time: String,
    val data: MetNorwayTimeSeriesData
)

data class MetNorwayTimeSeriesData(
    val instant: MetNorwayInstant,
    val next_1_hours: MetNorwayPeriod?,
    val next_6_hours: MetNorwayPeriod?
)

data class MetNorwayInstant(
    val details: MetNorwayDetails
)

data class MetNorwayDetails(
    val air_temperature: Double,
    val relative_humidity: Double,
    val wind_speed: Double,
    val wind_from_direction: Double,
    val wind_speed_of_gust: Double?,
    val air_pressure_at_sea_level: Double
)

data class MetNorwayPeriod(
    val summary: MetNorwaySummary
)

data class MetNorwaySummary(
    val symbol_code: String
)

// Map MET Norway symbol_code to our WMO-compatible codes
fun symbolCodeToWeatherCode(symbolCode: String): Int {
    val base = symbolCode.substringBefore("_") // remove _day/_night/_polartwilight
    return when (base) {
        "clearsky" -> 0
        "fair" -> 1
        "partlycloudy" -> 2
        "cloudy" -> 3
        "fog" -> 45
        "lightrain", "lightrainshowers" -> 61
        "rain", "rainshowers" -> 63
        "heavyrain", "heavyrainshowers" -> 65
        "lightsleet", "lightsleetshowers" -> 51
        "sleet", "sleetshowers" -> 53
        "heavysleet", "heavysleetshowers" -> 55
        "lightsnow", "lightsnowshowers" -> 71
        "snow", "snowshowers" -> 73
        "heavysnow", "heavysnowshowers" -> 75
        "lightrainandthunder", "lightrainshowersandthunder" -> 95
        "rainandthunder", "rainshowersandthunder" -> 95
        "heavyrainandthunder", "heavyrainshowersandthunder" -> 99
        "lightsleetandthunder", "sleetandthunder" -> 95
        "lightsnowandthunder", "snowandthunder" -> 95
        else -> 0
    }
}

fun MetNorwayResponse.toWeatherData(): WeatherData {
    val formatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val now = LocalDateTime.now()
    val current = properties.timeseries.minByOrNull { entry ->
        runCatching {
            val dt = LocalDateTime.parse(entry.time, formatter)
            Math.abs(java.time.Duration.between(now, dt).toMinutes())
        }.getOrElse { Long.MAX_VALUE }
    } ?: throw Exception("No forecast data available")
    val details = current.data.instant.details



    val symbolCode = current.data.next_1_hours?.summary?.symbol_code
        ?: current.data.next_6_hours?.summary?.symbol_code
        ?: "clearsky"

    return WeatherData(
        temperature = details.air_temperature,
        apparentTemperature = details.air_temperature, // MET Norway doesn't provide feels-like
        humidity = details.relative_humidity.toInt(),
        windSpeed = details.wind_speed,
        windDirection = details.wind_from_direction.toInt(),
        windGust = details.wind_speed_of_gust,
        pressure = details.air_pressure_at_sea_level,
        weatherCode = symbolCodeToWeatherCode(symbolCode),
        source = WeatherSource.MET_NORWAY
    )
}

fun MetNorwayResponse.toForecastData(): ForecastData {
    val now = LocalDateTime.now()
    val today = LocalDate.now()
    val formatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

    // hourly — keep 12 past + 48 future
    val hourlyList = properties.timeseries.mapNotNull { entry ->
        runCatching {
            val dt = LocalDateTime.parse(entry.time, formatter)
            val hoursFromNow = java.time.Duration.between(now, dt).toHours()
            if (hoursFromNow < -12 || hoursFromNow > 48) return@mapNotNull null
            val symbolCode = entry.data.next_1_hours?.summary?.symbol_code
                ?: entry.data.next_6_hours?.summary?.symbol_code
                ?: "clearsky"
            HourlyForecast(
                time = dt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                date = dt.toLocalDate().toString(),
                temperature = entry.data.instant.details.air_temperature,
                weatherCode = symbolCodeToWeatherCode(symbolCode),
                isCurrent = false
            )
        }.getOrNull()
    }

    // mark current hour
    val currentIndex = hourlyList.indexOfFirst {
        val dt = LocalDateTime.parse("${it.date}T${it.time}")
        val h = java.time.Duration.between(now, dt).toHours()
        h in -1..0
    }.coerceAtLeast(0)
    val hourly = hourlyList.mapIndexed { i, h ->
        h.copy(isCurrent = i == currentIndex)
    }

    // daily — aggregate from 6-hourly entries
    val dailyMap = mutableMapOf<LocalDate, MutableList<Double>>()
    val dailyCodeMap = mutableMapOf<LocalDate, Int>()
    properties.timeseries.forEach { entry ->
        runCatching {
            val dt = LocalDateTime.parse(entry.time, formatter)
            val date = dt.toLocalDate()
            val daysFromNow = java.time.temporal.ChronoUnit.DAYS.between(today, date)
            if (daysFromNow < -3 || daysFromNow > 7) return@forEach
            val temp = entry.data.instant.details.air_temperature
            dailyMap.getOrPut(date) { mutableListOf() }.add(temp)
            if (!dailyCodeMap.containsKey(date)) {
                val symbolCode = entry.data.next_6_hours?.summary?.symbol_code
                    ?: entry.data.next_1_hours?.summary?.symbol_code
                    ?: "clearsky"
                dailyCodeMap[date] = symbolCodeToWeatherCode(symbolCode)
            }
        }
    }

    val dailyList = dailyMap.entries.sortedBy { it.key }.mapNotNull { (date, temps) ->
        runCatching {
            DailyForecast(
                date = date.toString(),
                dayOfWeek = if (date == today) "Today"
                else date.dayOfWeek.getDisplayName(
                    java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()
                ),
                tempDay = temps.max(),
                tempNight = temps.min(),
                weatherCode = dailyCodeMap[date] ?: 0,
                isCurrent = date == today
            )
        }.getOrNull()
    }

    return ForecastData(hourly = hourly, daily = dailyList)
}
