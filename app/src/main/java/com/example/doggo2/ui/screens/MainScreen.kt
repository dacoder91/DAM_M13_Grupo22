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
import com.example.doggo2.ui.navigation.BottomNavItem
import com.example.doggo2.ui.screens.ui.theme.BottomNavigationBar

// Función principal que define la pantalla principal de la aplicación
@Composable
fun MainScreen(
    navController: NavHostController,
    parentNavController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Se establece el fondo de la pantalla principal
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
        // Se establece el contenido de la pantalla principal
    ) { innerPadding ->
        // Se define la navegación entre las diferentes pantallas de la aplicación
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(navController, parentNavController)
            }
            composable(BottomNavItem.Eventos.route) {
                EventosScreen(navController, parentNavController)
            }
            composable(BottomNavItem.Mapa.route) {
                MapaScreen(navController, parentNavController)
            }
            composable(BottomNavItem.Perdidos.route) {
                MascotasPerdidasScreen(navController, parentNavController)
            }
            composable(BottomNavItem.Perfil.route) {
                ProfileScreen(navController, parentNavController)
            }
        }
    }
}