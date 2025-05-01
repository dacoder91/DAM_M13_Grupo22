package com.example.doggo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doggo.R
import com.example.doggo.models.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

@Composable
fun HomeScreen(
    navController: NavController,
    parentNavController: NavController
) {
    val auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()
    var usuario by remember { mutableStateOf<Usuario?>(null) }

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

        // Botón "Salir"
        Button(
            onClick = {
                auth.signOut()
                parentNavController.navigate("login") {
                    popUpTo(0)
                }
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
                painter = painterResource(id = R.drawable.iconohome),
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

@Composable
fun ActionButton(
    text: String,
    iconRes: Int,
    bgColor: Color,
    textStyle: TextStyle,
    modifier: Modifier,
    shape: RoundedCornerShape,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = shape
    ) {
        Text(text, style = textStyle)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White
        )
    }
}