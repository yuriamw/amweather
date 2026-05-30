package com.amweather.amweather.data

import java.util.UUID

data class Location(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)
