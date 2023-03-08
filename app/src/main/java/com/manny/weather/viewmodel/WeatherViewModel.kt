package com.manny.weather.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haroldadmin.cnradapter.NetworkResponse
import com.manny.weather.data.WeatherData
import com.manny.weather.network.OpenWeatherMapApi
import kotlinx.coroutines.launch

class WeatherViewModel: ViewModel() {

    private val weatherMapApi = OpenWeatherMapApi.create()

    private val _weatherData = mutableStateOf<WeatherData?>(null)
    val weatherData: State<WeatherData?> = _weatherData

    // Added this since I was unable to figure out how to observe [_weatherData]
    // which is of type [MutableState], this is only used in the [MainActivity]
    private val _weatherLiveData = MutableLiveData<WeatherData?>(null)
    val weatherLiveData: LiveData<WeatherData?> = _weatherLiveData

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _searchCity = mutableStateOf("")
    val searchCity: State<String> = _searchCity

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun getWeather() {
        viewModelScope.launch {
            _loading.value = true

            when (val response = weatherMapApi.getWeather(_searchCity.value.trim())) {
                is NetworkResponse.Success -> {
                    _errorMessage.value = null
                    _weatherData.value = response.body
                    _weatherLiveData.value = response.body
                }
                is NetworkResponse.Error -> {
                    val errorMessage = when(response) {
                        is NetworkResponse.ServerError -> {
                            "Server Error"
                        }
                        is NetworkResponse.NetworkError -> {
                            "Network Error, please check your internet connection"
                        }
                        is NetworkResponse.UnknownError -> {
                            "Unknown Error: ${response.error.localizedMessage}"
                        }
                    }
                    setErrorMessage(errorMessage)
                }
            }

            _loading.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    fun setSearchCity(city: String) {
        _searchCity.value = city
    }
}