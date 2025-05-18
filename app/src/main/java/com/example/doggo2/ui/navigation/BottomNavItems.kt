
package com.example.doggo2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.doggo2.R

data class BottomNavItemData(
    val title: String,
    val icon: Painter,
    val route: String
)

@Composable
fun getBottomNavItems(): List<BottomNavItemData> {
    return listOf(
        BottomNavItemData("Home", painterResource(R.drawable.ic_home), "home"),
        BottomNavItemData("Eventos", painterResource(R.drawable.ic_eventos), "eventos"),
        BottomNavItemData("Mapa", painterResource(R.drawable.ic_mapa), "mapa"),
        BottomNavItemData("Perdidos", painterResource(R.drawable.ic_perdidos), "perdidos"),
        BottomNavItemData("Perfil", painterResource(R.drawable.ic_perfil), "perfil")
    )
}
