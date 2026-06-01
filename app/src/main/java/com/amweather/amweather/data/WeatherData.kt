package com.amweather.amweather.data

// Source-agnostic weather model
// Both Open-Meteo and MET Norway map into this
data class WeatherData(
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val pressure: Double,
    val weatherCode: Int,
    val source: WeatherSource
)

enum class WeatherSource {
    OPEN_METEO,
    MET_NORWAY
}
