
package com.amweather.amweather.data

import java.time.LocalTime

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
