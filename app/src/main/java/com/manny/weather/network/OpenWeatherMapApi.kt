package com.manny.weather.network

import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.manny.weather.data.WeatherData
import com.manny.weather.data.WeatherErrorResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapApi {

    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String = API_KEY
    ): NetworkResponse<WeatherData, WeatherErrorResponse>

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        private const val IMAGE_BASE_URL = "https://openweathermap.org/img/wn/"
        const val API_KEY = "1eee0f3d7b13530ff512286a40520938"

        fun getIconImageUrl(iconName: String): String {
            return "${IMAGE_BASE_URL}$iconName@2x.png"
        }

        fun create(): OpenWeatherMapApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(NetworkResponseAdapterFactory())
                .build()
                .create()
        }
    }

}