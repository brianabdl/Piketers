package com.brianabdl.piketers.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brianabdl.piketers.data.preferences.SettingsManager
import com.brianabdl.piketers.data.repository.PiketRepository
import com.brianabdl.piketers.ui.screens.HomeScreen
import com.brianabdl.piketers.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    settingsManager: SettingsManager,
    piketRepository: PiketRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                piketRepository = piketRepository,
                navigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                settingsManager = settingsManager,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}