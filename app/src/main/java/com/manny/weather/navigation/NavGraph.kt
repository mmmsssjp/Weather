package com.manny.weather.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.manny.weather.ui.HomeScreen
import com.manny.weather.viewmodel.WeatherViewModel

@Composable
fun NavGraph (
    viewModel: WeatherViewModel,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screens.Home.route)
    {
        composable(route = Screens.Home.route){
            HomeScreen(viewModel)
        }
    }
}
