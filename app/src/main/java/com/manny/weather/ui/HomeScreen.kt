package com.manny.weather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.manny.weather.R
import com.manny.weather.data.Main
import com.manny.weather.data.Weather
import com.manny.weather.data.WeatherData
import com.manny.weather.network.OpenWeatherMapApi
import com.manny.weather.viewmodel.WeatherViewModel
import java.math.RoundingMode
import java.text.DecimalFormat

@Composable
fun HomeScreen(viewModel: WeatherViewModel) {
    val data = viewModel.weatherData.value

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        item {
            CitySearch(viewModel)
        }

        if (data == null) {
            item {
                NothingLoaded()
            }
            return@LazyColumn
        }
        item {
            City(data)
        }

        item {
            MainWeather(data.main)
        }

        itemsIndexed(
            items = data.weather,
            key = { _, weather -> weather.id },
        ) { _, weather ->
            WeatherRow(weather)
        }
    }
}

@Composable
private fun NothingLoaded() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
    ) {
        Text(
            text = "Nothing is loaded, please input a city in the input field above",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun City(data: WeatherData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
    ) {
        Text(
            text = "${data.name} Weather",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

    }
}

@Composable
private fun MainWeatherInfoRow(name: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = name,
        )
        Text(
            text = value,
        )
    }
}

@Composable
private fun MainWeather(main: Main) {
    MainWeatherInfoRow(name = "Temperature", value = main.temp.kelvinToFahrenheit())
    MainWeatherInfoRow(name = "Feels like ", value = main.feels_like.kelvinToFahrenheit())
    MainWeatherInfoRow(name = "Humidity ", value = main.humidity.toHumidity())
    MainWeatherInfoRow(name = "Min Temp ", value = main.temp_min.kelvinToFahrenheit())
    MainWeatherInfoRow(name = "Max Temp ", value = main.temp_max.kelvinToFahrenheit())
    MainWeatherInfoRow(name = "Pressure ", value = main.pressure.toPressure())
}

@Composable
private fun WeatherRow(weather: Weather) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
    ) {
        AsyncImage(
            model = OpenWeatherMapApi.getIconImageUrl(weather.icon),
            contentDescription = "${weather.main}, ${weather.description}",
            placeholder = painterResource(id = R.drawable.baseline_downloading_24),
            error =  painterResource(id = R.drawable.baseline_error_24),

        )
        Column (
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = weather.main,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = weather.description,
            )
        }

    }
}

@Composable
private fun CitySearch(viewModel: WeatherViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextField(
            modifier = Modifier.
                background(Color.Black),
            value = viewModel.searchCity.value,
            onValueChange = {
                viewModel.setSearchCity(it)
            },
            label = { Text("Input city name") },
            colors =  TextFieldDefaults.textFieldColors(Color.White),
            enabled = !viewModel.loading.value
        )
        Button(
            modifier = Modifier
                .wrapContentSize(),
            onClick = {
                viewModel.getWeather()
            },
            enabled = !viewModel.loading.value
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize(),
                text = "Fetch",
                color = Color.White
            )
        }
    }
}

private fun Double.kelvinToFahrenheit(): String {
    val f = ((this - 273.15) * 1.8) + 32
    val df = DecimalFormat("#")
    df.roundingMode = RoundingMode.HALF_DOWN
    return "${df.format(f)} F"
}

private fun Int.toPressure(): String {
    return "$this hPa"
}

private fun Int.toHumidity(): String {
    return "$this %"
}
