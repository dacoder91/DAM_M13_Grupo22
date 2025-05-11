package com.example.doggo2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doggo2.ui.screens.LoginScreen
import com.example.doggo2.ui.screens.MainScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

// Función principal de navegación de la aplicación
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

// Definición de los elementos del menú de navegación inferior
sealed class BottomNavItem(val title: String, val icon: ImageVector, val route: String) {
    object Home : BottomNavItem("Home", Icons.Filled.Home, "home")
    object Eventos : BottomNavItem("Eventos", Icons.Filled.Event, "eventos")
    object Mapa : BottomNavItem("Mapa", Icons.Filled.Map, "mapa")
    object Perdidos : BottomNavItem("Perdidos", Icons.Filled.Pets, "perdidos")
    object Perfil : BottomNavItem("Perfil", Icons.Filled.Person, "perfil")

    companion object {
        val items = listOf(Home, Eventos, Mapa, Perdidos, Perfil)
    }
}