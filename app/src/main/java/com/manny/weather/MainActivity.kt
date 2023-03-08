package com.manny.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.*
import com.manny.weather.navigation.NavGraph
import com.manny.weather.ui.ErrorDialog
import com.manny.weather.ui.theme.WeatherTheme
import com.manny.weather.viewmodel.WeatherViewModel
import java.util.*


class MainActivity : ComponentActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[WeatherViewModel::class.java] }

    private val sharedPreference by lazy { getSharedPreferences("LAST_SUCCESSFULLY_SEARCHED_CITY", Context.MODE_PRIVATE) }

    private val fusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    // hack to keep location from updating more than once
    private var locationFound = false

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // wasn't able to get location properly updating but I tried :)
        startLocationPermissionRequest()
        requestLocationUpdate()

        getLastSuccessfullySearchedCity()

        setContent {
            WeatherTheme {
                ErrorDialog(viewModel)

                val refreshing by remember { viewModel.loading }
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = refreshing,
                    onRefresh = { viewModel.getWeather() }
                )
                val navController = rememberNavController()

                // A surface container using the 'background' color from the theme
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        TopAppBar(
                            title = { Text(text = "Weather App") }
                        )
                        NavGraph(
                            viewModel = viewModel,
                            navController = navController,
                        )
                    }
                    PullRefreshIndicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        state = pullRefreshState,
                        refreshing = refreshing
                    )
                }
            }
        }

        viewModel.weatherLiveData.observe(this) { weatherData ->
            if (weatherData != null) {
                saveLastSuccessfullySearchedCity()
            }
        }
    }

    private fun saveLastSuccessfullySearchedCity() {
        sharedPreference.edit().apply {
            putString(getString(R.string.last_saved_city), viewModel.searchCity.value)
            apply()
        }
    }

    private fun getLastSuccessfullySearchedCity() {
        val lastSavedSearchCity = sharedPreference.getString(getString(R.string.last_saved_city), "") ?: ""
        viewModel.setSearchCity(lastSavedSearchCity)
        viewModel.getWeather()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationPermissionRequest() {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    location ?: return@addOnSuccessListener
                    val longitude: Double = location.longitude
                    val latitude: Double = location.latitude

                    val geocoder = Geocoder(this, Locale.getDefault())
                    geocoder.getFromLocation(latitude, longitude, 1)?.first()?.let {
                        val cityName: String = it.getAddressLine(0)
                        viewModel.setSearchCity(cityName)
                        viewModel.getWeather()
                    }
                }
            } else {
                viewModel.setErrorMessage("Location permission denied, please allow for a better app experience")
            }
        }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate() {
        val locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20 * 1000);
        val locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationFound) return
                locationResult.locations.first()?.let {location ->
                    val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)?.first()?.let {
                        val cityName: String = it.getAddressLine(0)
                        viewModel.setSearchCity(cityName)
                        viewModel.getWeather()
                        locationFound = true
                    }
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

}
