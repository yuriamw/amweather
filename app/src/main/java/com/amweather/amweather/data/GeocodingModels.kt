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
