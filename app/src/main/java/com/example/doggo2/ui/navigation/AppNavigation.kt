package com.example.doggo2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doggo2.ui.screens.LoginScreen
import com.example.doggo2.ui.screens.MainScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("main") {
            val nestedNavController = rememberNavController()
            MainScreen(
                navController = nestedNavController,
                parentNavController = navController)
        }
    }
}
