package com.amweather.amweather.data

// MET Norway Locationforecast 2.0 compact response structure
// https://api.met.no/weatherapi/locationforecast/2.0/documentation

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
    val current = properties.timeseries.firstOrNull()
        ?: throw Exception("No forecast data available")
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
        pressure = details.air_pressure_at_sea_level,
        weatherCode = symbolCodeToWeatherCode(symbolCode),
        source = WeatherSource.MET_NORWAY
    )
}
