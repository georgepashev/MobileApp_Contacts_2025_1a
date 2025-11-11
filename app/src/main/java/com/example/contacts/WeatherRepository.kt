package com.example.contacts

class WeatherRepository(
    public val api: OpenWeatherApi = WeatherNetwork.api
) {
    suspend fun getCurrent(lat: Double, lon: Double) = api.current(lat, lon)
}