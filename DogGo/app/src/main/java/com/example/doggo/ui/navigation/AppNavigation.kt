package com.example.doggo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.doggo.ui.screens.LoginScreen
import com.example.doggo.ui.screens.*

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("main") { MainScreen(navController) }
        composable("eventos") { EventosScreen() }
        composable("mapa") { MapaScreen() }
        composable("perdidos") { MascotasPerdidasScreen() }
        composable("perfil") { ProfileScreen(navController) }
    }
}