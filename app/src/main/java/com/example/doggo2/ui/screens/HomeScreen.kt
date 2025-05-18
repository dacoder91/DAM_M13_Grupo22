package com.example.doggo2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doggo2.R
import com.example.doggo2.ui.components.LogoutButton
import com.example.doggo2.controller.ActionButton
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Icono principal (reutiliza el avatar del perfil)
            Image(
                painter = painterResource(id = R.drawable.iconohome1),
                contentDescription = "Icono central",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Texto de bienvenida
            Text(
                text = "Bienvenido/a a DogGo \n${usuario?.nombre ?: "Usuario"}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily(Font(R.font.yellowpeach)) // asegúrate de tenerlo importado correctamente
                ),
                textAlign = TextAlign.Center,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botones de acción
            val buttonColor = Color(0xFFE91E63)
            val buttonModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(50.dp)

            val buttonShape = RoundedCornerShape(20.dp)
            val textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)

            //a continuacion se crean los botones de la pantalla de inicio
            ActionButton("Eventos", R.drawable.iconoeventos, buttonColor, textStyle, buttonModifier, buttonShape) {
                navController.navigate("eventos")
            }

            ActionButton("Mapa", R.drawable.iconomapa, buttonColor, textStyle, buttonModifier, buttonShape) {
                navController.navigate("mapa")
            }

            ActionButton("Perdidos", R.drawable.iconoperdidos, buttonColor, textStyle, buttonModifier, buttonShape) {
                navController.navigate("perdidos")
            }

            ActionButton("Perfil", R.drawable.iconoperfil, buttonColor, textStyle, buttonModifier, buttonShape) {
                navController.navigate("perfil")
            }
        }
    }
}

