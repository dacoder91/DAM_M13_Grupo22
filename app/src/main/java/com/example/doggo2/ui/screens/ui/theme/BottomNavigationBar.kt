package com.example.doggo2.ui.screens.ui.theme


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.doggo2.ui.navigation.BottomNavItem

// Aqui se define la barra de navegación inferior que se mostrará en la pantalla principal de la aplicación.
@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        BottomNavItem.items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    try {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    } catch (e: Exception) {
                        println("Error al navegar a la ruta ${item.route}: ${e.message}")
                    }
                }
            )
        }
    }
}
