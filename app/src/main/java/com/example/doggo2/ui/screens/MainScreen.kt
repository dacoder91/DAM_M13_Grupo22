package com.example.doggo2.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.doggo2.ui.screens.ui.theme.BottomNavigationBar

// Función principal que define la pantalla principal de la aplicación
@Composable
fun MainScreen(
    navController: NavHostController,
    parentNavController: NavHostController
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    )
    { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding) // ← Añadido
        ) {
        composable("home") {
                HomeScreen(navController, parentNavController)
            }
            composable("eventos") { EventosScreen(navController, parentNavController) }
            composable("mapa") { MapaScreen(navController, parentNavController) }
            composable("perdidos") { MascotasPerdidasScreen(navController, parentNavController) }
            composable("perfil") { ProfileScreen(navController, parentNavController) }
        }
    }
}