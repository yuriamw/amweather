package com.amweather.amweather.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {

    private val api: WeatherApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(WeatherApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    suspend fun fetchWeather(
        lat: Double = WeatherApi.DEFAULT_LAT,
        lon: Double = WeatherApi.DEFAULT_LON
    ): Result<WeatherResponse> = runCatching {
        api.getCurrentWeather(latitude = lat, longitude = lon)
    }
}
