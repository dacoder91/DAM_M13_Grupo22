package com.example.doggo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.doggo.R
import com.example.doggo.ui.screens.ui.theme.YellowPeach
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun EventosScreen(
    navController: NavController,
    parentNavController: NavController
) {
    val auth = Firebase.auth

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo difuminado
        Image(
            painter = painterResource(id = R.drawable.imageneventos2),
            contentDescription = "Fondo difuminado",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        // Botón de salir
        Button(
            onClick = {
                auth.signOut()
                parentNavController.navigate("login") { popUpTo(0) }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(width = 110.dp, height = 35.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE91E63),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Salir")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.iconoeventos),
                contentDescription = "Icono central",
                modifier = Modifier
                    .size(96.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Eventos",
                fontFamily = YellowPeach,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Aquí iría el contenido real de la pantalla de eventos
            Text(
                text = "Aquí aparecerán tus próximos eventos.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
