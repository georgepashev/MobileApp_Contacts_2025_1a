package com.example.contacts

import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val name: String? = null,
    val weather: List<Wx>? = emptyList(),
    val main: Main? = null
) {
    data class Wx(
        val description: String? = null,
        val icon: String? = null
    )

    data class Main(
        val temp: Double? = null,
        val feels_like: Double? = null,
        val humidity: Int? = null
    )
}

interface OpenWeatherApi {
    @GET("data/2.5/weather")
    suspend fun current(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): WeatherResponse
}