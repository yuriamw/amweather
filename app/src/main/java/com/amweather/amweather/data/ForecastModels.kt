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

data class HourlyForecast(
    val time: String,           // "14:00"
    val date: String,           // "2026-06-01"
    val temperature: Double,
    val weatherCode: Int,
    val isCurrent: Boolean = false
)

data class DailyForecast(
    val date: String,           // "2026-06-01"
    val dayOfWeek: String,      // "Mon"
    val tempDay: Double,
    val tempNight: Double,
    val weatherCode: Int,
    val isCurrent: Boolean = false
)

data class ForecastData(
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)

const val HOURLY_PAST_HOURS = 12L
const val HOURLY_FUTURE_HOURS = 48L
const val DAILY_PAST_DAYS = 3L
const val DAILY_FUTURE_DAYS = 7L

fun mergeMETNorwayForecast(cached: ForecastData?, fresh: ForecastData): ForecastData {
    val now = java.time.LocalDateTime.now()
    val today = java.time.LocalDate.now()

    fun key(h: HourlyForecast) = "${h.date}T${h.time}"
    fun hoursDiff(h: HourlyForecast) =
        java.time.Duration.between(now, java.time.LocalDateTime.parse(key(h))).toHours()
    fun daysDiff(d: DailyForecast) =
        java.time.temporal.ChronoUnit.DAYS.between(today, java.time.LocalDate.parse(d.date))

    // extract past entries within boundary eagerly — guards against stale cache content
    val pastHourly = (cached?.hourly ?: emptyList())
        .filter { hoursDiff(it) in -HOURLY_PAST_HOURS..-1 }
    val mergedHourly = (fresh.hourly + pastHourly)
        .distinctBy { key(it) }
        .sortedBy { key(it) }
        .filter { hoursDiff(it) in -HOURLY_PAST_HOURS..HOURLY_FUTURE_HOURS }

    val currentIdx = mergedHourly.indexOfFirst { hoursDiff(it) in -1..0 }.coerceAtLeast(0)
    val hourly = mergedHourly.mapIndexed { i, h -> h.copy(isCurrent = i == currentIdx) }

    val pastDaily = (cached?.daily ?: emptyList())
        .filter { daysDiff(it) in -DAILY_PAST_DAYS..-1 }
    val daily = (fresh.daily + pastDaily)
        .distinctBy { it.date }
        .sortedBy { it.date }
        .filter { daysDiff(it) in -DAILY_PAST_DAYS..DAILY_FUTURE_DAYS }

    return ForecastData(hourly = hourly, daily = daily)
}
