package com.example.doggo2.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.doggo2.ui.navigation.BottomNavItem

@Composable
fun MainScreen(
    navController: NavHostController,
    parentNavController: NavHostController // para volver al login
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItem.items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
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

