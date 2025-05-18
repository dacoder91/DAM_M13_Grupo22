package com.example.doggo2.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.doggo2.R
import com.example.doggo2.ui.components.CustomButton
import com.example.doggo2.ui.components.LogoutButton
import com.example.doggo2.models.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

//Función para mostrar la pantalla de inicio
@Composable
fun HomeScreen(
    navController: NavController,
    parentNavController: NavController
) {
    val auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()
    var usuario by remember { mutableStateOf<Usuario?>(null) }

    // Obtener el usuario actual de Firebase Auth
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val usuarioId = currentUser.uid
            db.collection("usuarios").document(usuarioId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        usuario = document.toObject(Usuario::class.java)
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo difuminada
        Image(
            painter = painterResource(id = R.drawable.imagenhome),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5f
        )

        // Botón Salir
        LogoutButton(
            modifier = Modifier.align(Alignment.TopEnd),
            parentNavController = parentNavController
        )


        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Icono principal (reutiliza el avatar del perfil)
            Image(
                painter = painterResource(id = R.drawable.iconohome1),
                contentDescription = "Icono central",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            )

            Spacer(modifier = Modifier.height(45.dp))

            // Texto de bienvenida
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido/a a",
                    fontSize = 28.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "DogGo",
                    fontSize = 48.sp,
                    fontFamily = FontFamily(Font(R.font.yellowpeach)),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Text(
                    text = usuario?.nombre ?: "Usuario",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }


            Spacer(modifier = Modifier.weight(0.5f))

            // Botones de acción
            CustomButton(
                text = "Eventos",
                icon = painterResource(id = R.drawable.ic_eventos),
                onClick = { navController.navigate("eventos") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp)
            )
            CustomButton(
                text = "Mapa",
                icon = painterResource(id = R.drawable.ic_mapa),
                onClick = { navController.navigate("mapa") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp)
            )
            CustomButton(
                text = "Perdidos",
                icon = painterResource(id = R.drawable.ic_perdidos),
                onClick = { navController.navigate("perdidos") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp)
            )
            CustomButton(
                text = "Perfil",
                icon = painterResource(id = R.drawable.ic_perfil),
                onClick = { navController.navigate("perfil") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp)
            )

            Spacer(modifier = Modifier.weight(0.5f)) // algo de margen inferior
        }
    }
}

