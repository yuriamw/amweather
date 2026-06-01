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

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {

    companion object {
        const val USER_AGENT = "AMWeather/1.0 github.com/yuriamw/amweather"
    }

    private val openMeteoApi: WeatherApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(WeatherApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    private val metNorwayApi: MetNorwayApiService by lazy {
        MetNorwayApi.create(USER_AGENT)
    }

    private val metNorwaySunriseApi: MetNorwaySunriseService by lazy {
        MetNorwaySunriseApi.create(USER_AGENT)
    }

    suspend fun fetchWeather(
        lat: Double,
        lon: Double,
        source: WeatherSource = WeatherSource.OPEN_METEO
    ): Result<WeatherData> = runCatching {
        when (source) {
            WeatherSource.OPEN_METEO ->
                openMeteoApi.getCurrentWeather(latitude = lat, longitude = lon)
                    .toWeatherData()
            WeatherSource.MET_NORWAY ->
                metNorwayApi.getForecast(
                    latitude = "%.4f".format(lat).toDouble(),
                    longitude = "%.4f".format(lon).toDouble()
                ).toWeatherData()
        }
    }

    suspend fun fetchSunMoon(
        lat: Double,
        lon: Double
    ): Result<SunMoonData?> = runCatching {
        // get sunrise/sunset from Open-Meteo
        val sunData = openMeteoApi.getDailyData(
            latitude = lat,
            longitude = lon
        ).toSunMoonData() ?: return@runCatching null

        // get moon data from MET Norway
        val date = todayDate()
        val offset = utcOffset()
        val roundedLat = "%.4f".format(lat).toDouble()
        val roundedLon = "%.4f".format(lon).toDouble()

        val moonData = runCatching {
            metNorwaySunriseApi.getMoon(roundedLat, roundedLon, date, offset)
        }.getOrNull()

        val moonrise = moonData?.properties?.moonrise?.time?.let { parseTimeFromIso(it) }
        val moonset = moonData?.properties?.moonset?.time?.let { parseTimeFromIso(it) }
        val moonPhase = moonData?.properties?.moonphase

        sunData.withMoonData(moonrise, moonset, moonPhase)
    }

    private fun todayDate(): String =
        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)

    private fun utcOffset(): String {
        val offset = java.time.ZoneId.systemDefault()
            .rules.getOffset(java.time.Instant.now())
        val totalMinutes = offset.totalSeconds / 60
        val hours = totalMinutes / 60
        val minutes = kotlin.math.abs(totalMinutes % 60)
        return "%+03d:%02d".format(hours, minutes)
    }

    suspend fun fetchForecast(
        lat: Double,
        lon: Double,
        source: WeatherSource
    ): Result<ForecastData> = runCatching {
        when (source) {
            WeatherSource.OPEN_METEO ->
                openMeteoApi.getForecast(latitude = lat, longitude = lon)
                    .toForecastData()
            WeatherSource.MET_NORWAY ->
                metNorwayApi.getForecast(
                    latitude = "%.4f".format(lat).toDouble(),
                    longitude = "%.4f".format(lon).toDouble()
                ).toForecastData()
        }
    }
}
