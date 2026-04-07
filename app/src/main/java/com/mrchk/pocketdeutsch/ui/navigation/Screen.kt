package com.mrchk.pocketdeutsch.ui.navigation

sealed class Screen (val route: String) {
    object Home: Screen("home_screen")
    object Dictionary : Screen("dictionary_screen")
    object Test: Screen("test_screen")
    object Settings: Screen("settings_screen")
    object Profile : Screen("profile_screen")
}