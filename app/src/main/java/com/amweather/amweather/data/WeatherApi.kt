package com.amweather.amweather.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("wind_speed_unit") windSpeedUnit: String = "ms"  // metres per second
    ): WeatherResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"

        // Fields we request from the API — keep this minimal
        const val CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m," +
                    "wind_speed_10m,wind_direction_10m,surface_pressure,weather_code"

        // Kharkiv coordinates
        const val DEFAULT_LAT = 49.9935
        const val DEFAULT_LON = 36.2304
    }
}
