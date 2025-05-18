package com.example.doggo2.ui.screens.ui.theme

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.doggo2.ui.navigation.getBottomNavItems

// Aqui se define la barra de navegación inferior que se mostrará en la pantalla principal de la aplicación.
@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String?) {
    if (currentRoute == "home") return // oculta la barra en HomeScreen

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 4.dp,
        modifier = Modifier
            .height(95.dp) //
    ) {
        val items = getBottomNavItems()
        items.forEach { item ->

            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(30.dp), //
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontFamily = YellowPeach,
                        fontSize = 11.sp, //
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

