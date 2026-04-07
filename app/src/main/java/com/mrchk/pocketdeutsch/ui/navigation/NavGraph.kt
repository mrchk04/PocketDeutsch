package com.mrchk.pocketdeutsch.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ){
        composable(Screen.Home.route) {
            PlaceholderScreen("Main", PocketTheme.colors.primary)
        }
    }
}

@Composable
fun PlaceholderScreen(name: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketTheme.colors.paper),
        contentAlignment = Alignment.Center,
    ){
        Text(
            text = name,
            style = PocketTheme.typography.headlineLarge,
            color = color
        )
    }
}