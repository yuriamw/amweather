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
