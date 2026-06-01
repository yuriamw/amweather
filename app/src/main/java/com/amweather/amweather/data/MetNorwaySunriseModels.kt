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

// MET Norway Sunrise 3.0 API response models
// https://docs.api.met.no/doc/formats/SunriseJSON.html

data class SunriseResponse(
    val properties: SunProperties
)

data class SunProperties(
    val body: String,
    val sunrise: SunEvent?,
    val sunset: SunEvent?,
    val solarnoon: SolarEvent?,
    val solarmidnight: SolarEvent?
)

data class SunEvent(
    val time: String,
    val azimuth: Double?
)

data class SolarEvent(
    val time: String,
    val disc_centre_elevation: Double?,
    val visible: Boolean?
)

data class MoonriseResponse(
    val properties: MoonProperties
)

data class MoonProperties(
    val body: String,
    val moonrise: MoonEvent?,
    val moonset: MoonEvent?,
    val moonphase: Double?   // plain number, not an object
)

data class MoonEvent(
    val time: String,
    val azimuth: Double?
)
