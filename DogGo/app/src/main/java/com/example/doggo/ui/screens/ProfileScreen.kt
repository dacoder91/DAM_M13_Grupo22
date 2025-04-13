package com.example.doggo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doggo.R
import com.example.doggo.models.Usuario
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = Firebase.auth
    var usuario by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo(0)
            }
        } else {
            usuario = Usuario(
                id = currentUser.uid,
                nombre = "Cristina",
                email = currentUser.email ?: "sin correo",
                telefono = "123 456 789",
                mascotas = listOf()
            )
        }
    }

    if (usuario == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3D471)) // Fondo suave
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar de usuario
        Image(
            painter = painterResource(id = R.drawable.perroypersona),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre del usuario actual
            Text(
                text = usuario!!.nombre,
                style = MaterialTheme.typography.titleLarge
            )

        Spacer(modifier = Modifier.height(24.dp))

        // CARD con los datos de usuario
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Column {
                    Text(text = "Mi Perfil", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Email: ${usuario!!.email}")
                    Text(text = "Teléfono: ${usuario!!.telefono}")
                }

                IconButton(onClick = {
                    // Aquí podremos abrir una pantalla para editar datos
                }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar perfil")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de mascotas
        Text(
            text="Mis Mascotas",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Las cards para cada mascota (de momento solo un placeholder)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Aquí aparecerán tus mascotas registradas")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón para cerrar sesión
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("login") {
                    popUpTo(0) // Limpia toda la pila de navegación
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Cerrar Sesión")
        }
    }
}