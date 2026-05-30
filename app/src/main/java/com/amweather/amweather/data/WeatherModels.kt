package com.amweather.amweather.data

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

fun weatherCodeToEmoji(code: Int): String = when (code) {
    0 -> "☀️"
    1 -> "🌤️"
    2 -> "⛅"
    3 -> "☁️"
    45, 48 -> "🌫️"
    51, 53, 55 -> "🌦️"
    61, 63 -> "🌧️"
    65 -> "🌧️"
    71, 73, 75 -> "❄️"
    77 -> "🌨️"
    80, 81, 82 -> "🌧️"
    85, 86 -> "🌨️"
    95 -> "⛈️"
    96, 99 -> "⛈️"
    else -> "🌡️"
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
