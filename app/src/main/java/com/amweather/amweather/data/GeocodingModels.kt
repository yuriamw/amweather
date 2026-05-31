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

data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

data class GeocodingResult(
    val id: Long,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val admin1: String?      // region/state, e.g. "Kharkiv Oblast"
) {
    fun toLocation() = Location(
        name = name,
        country = country,
        latitude = latitude,
        longitude = longitude
    )

    // friendly display name shown in search results
    val displayName: String get() = buildString {
        append(name)
        if (!admin1.isNullOrBlank()) append(", $admin1")
        append(", $country")
    }
}
