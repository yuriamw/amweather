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

    @GET("v1/forecast")
    suspend fun getDailyData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") sunrise: String = "sunrise",
        @Query("daily") sunset: String = "sunset",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 1
    ): DailyWeatherResponse

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourlyTemperature: String = "temperature_2m",
        @Query("hourly") hourlyWeatherCode: String = "weather_code",
        @Query("daily") dailyWeatherCode: String = "weather_code",
        @Query("daily") dailyTempMax: String = "temperature_2m_max",
        @Query("daily") dailyTempMin: String = "temperature_2m_min",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7,
        @Query("past_days") pastDays: Int = 3
    ): OpenMeteoForecastResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
        const val CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m," +
                    "wind_speed_10m,wind_direction_10m,surface_pressure,weather_code"
        const val DAILY_FIELDS = "sunrise,sunset"
        const val DEFAULT_LAT = 49.9935
        const val DEFAULT_LON = 36.2304
    }

}
