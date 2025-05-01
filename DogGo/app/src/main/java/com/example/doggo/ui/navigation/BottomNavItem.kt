package com.example.doggo.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

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
