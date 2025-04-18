package com.example.doggo.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(bottomNavController) }
    ) { innerPadding ->
        BottomNavGraph(
            navController = bottomNavController,
            parentNavController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem("Home", Icons.Filled.Home, "home"), // Nuevo ítem
        NavigationItem("Eventos", Icons.Filled.DateRange, "eventos"),
        NavigationItem("Mapa", Icons.Filled.Place, "mapa"),
        NavigationItem("Perdidos", Icons.Filled.Search, "perdidos"),
        NavigationItem("Perfil", Icons.Filled.AccountBox, "perfil")
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    parentNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home", // Cambiar la pantalla inicial a "home"
        modifier = modifier
    ) {
        composable("home") { HomeScreen(parentNavController) } // Nueva pantalla
        composable("eventos") { EventosScreen() }
        composable("mapa") { MapaScreen() }
        composable("perdidos") { MascotasPerdidasScreen(parentNavController) }
        composable("perfil") { ProfileScreen(parentNavController) }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)