package com.example.doggo.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.doggo.ui.navigation.BottomNavItem
import com.example.doggo.ui.screens.ui.theme.BottomNavigationBar

@Composable
fun MainScreen(
    navController: NavHostController,
    parentNavController: NavHostController // para volver al login
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    navController = navController,
                    parentNavController = parentNavController
                )
            }
            composable(BottomNavItem.Eventos.route) {
                EventosScreen(
                    navController = navController,
                    parentNavController = parentNavController
                )
            }
            composable(BottomNavItem.Mapa.route) {
                MapaScreen(
                    navController = navController,
                    parentNavController = parentNavController
                )
            }
            composable(BottomNavItem.Perdidos.route) {
                MascotasPerdidasScreen(
                    navController = navController,
                    parentNavController = parentNavController
                )
            }
            composable(BottomNavItem.Perfil.route) {
                ProfileScreen(
                    navController = navController,
                    parentNavController = parentNavController
                )
            }
        }
    }
}

