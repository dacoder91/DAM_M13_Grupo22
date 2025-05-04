package com.example.doggo2.ui.screens.ui.theme


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.doggo2.ui.navigation.BottomNavItem

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
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
