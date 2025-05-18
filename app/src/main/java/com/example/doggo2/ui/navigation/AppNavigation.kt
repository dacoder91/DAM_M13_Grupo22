package com.example.doggo2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doggo2.ui.screens.LoginScreen
import com.example.doggo2.ui.screens.MainScreen
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.doggo2.R

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
sealed class BottomNavItem(val title: String, val icon: Painter, val route: String) {
    companion object {
        @Composable
        fun items() = listOf(
            BottomNavItemData("Home", painterResource(id = R.drawable.ic_home), "home"),
            BottomNavItemData("Eventos", painterResource(id = R.drawable.ic_eventos), "eventos"),
            BottomNavItemData("Mapa", painterResource(id = R.drawable.ic_mapa), "mapa"),
            BottomNavItemData("Perdidos", painterResource(id = R.drawable.ic_perdidos), "perdidos"),
            BottomNavItemData("Perfil", painterResource(id = R.drawable.ic_perfil), "perfil")
        )
    }
}

