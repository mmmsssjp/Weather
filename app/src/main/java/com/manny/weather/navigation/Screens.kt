package com.manny.weather.navigation

sealed class Screens(val route: String) {
    object Home : Screens("home")
}