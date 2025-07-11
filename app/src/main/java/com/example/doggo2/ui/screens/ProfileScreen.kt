package com.example.doggo2.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.doggo2.R
import com.example.doggo.models.Mascota
import com.example.doggo2.controller.calculateAge
import com.example.doggo2.models.Usuario
import com.example.doggo2.ui.components.LogoutButton
import com.example.doggo2.ui.screens.ui.theme.YellowPeach
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.doggo2.ui.components.CustomButton
import kotlinx.coroutines.tasks.await

// Pantalla principal del perfil del usuario. Muestra la información del usuario,
// la lista de mascotas asociadas y permite editar el perfil, añadir o editar mascotas.
@Composable
fun ProfileScreen(
    navController: NavController,
    parentNavController: NavController
) {
    val context = LocalContext.current
    val auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()

    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var mascotas by remember { mutableStateOf<List<Mascota>>(emptyList()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddPetDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var selectedPet by remember { mutableStateOf<Mascota?>(null) }

    // Verifica si el usuario está autenticado
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo(0)
            }
        } else {
            val usuarioId = currentUser.uid
            try {
                val usuarioDocument = db.collection("usuarios").document(usuarioId).get().await()
                if (usuarioDocument.exists()) {
                    val usuarioData = usuarioDocument.toObject(Usuario::class.java)
                    if (usuarioData != null) {
                        usuario = usuarioData.copy(id = usuarioDocument.id) // Asigna el ID del documento al campo id
                    }

                    val mascotasQuery = db.collection("mascotas")
                        .whereEqualTo("usuarioId", usuarioId)
                        .get()
                        .await()
                    val mascotasList = mascotasQuery.documents.mapNotNull { document ->
                        document.toObject(Mascota::class.java)?.copy(id = document.id)
                    }
                    mascotas = mascotasList
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Diálogo para editar el perfil del usuario
    if (showEditDialog && usuario != null) {
        EditProfileDialog(
            usuario = usuario!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedUsuario ->
                usuario = updatedUsuario
                showEditDialog = false
            }
        )
    }

    // Diálogo para añadir o editar mascotas
    if (showAddPetDialog && usuario != null) {
        AddPetDialog(
            usuarioId = usuario!!.id,
            onDismiss = { showAddPetDialog = false },
            onSave = { newPet ->
                val petWithUserId = newPet.copy(usuarioId = usuario!!.id)

                try {
                    db.collection("mascotas")
                        .add(petWithUserId)
                        .addOnSuccessListener { documentReference ->
                            val updatedMascotas = mascotas + petWithUserId.copy(id = documentReference.id)
                            mascotas = updatedMascotas

                            db.collection("usuarios").document(usuario!!.id)
                                .update("mascotas", updatedMascotas.map { it.id })
                                .addOnSuccessListener {
                                    showAddPetDialog = false
                                }
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al guardar la mascota: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Diálogo para editar una mascota existente
    if (showEditPetDialog && selectedPet != null) {
        EditPetDialog(
            mascota = selectedPet!!,
            onDismiss = { showEditPetDialog = false },
            onSave = { updatedPet ->
                try {
                    db.collection("mascotas").document(updatedPet.id)
                        .set(updatedPet)
                        .addOnSuccessListener {
                            mascotas = mascotas.map { if (it.id == updatedPet.id) updatedPet else it }
                            showEditPetDialog = false
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al actualizar la mascota: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Imagen de fondo de pantalla
        Image(
            painter = painterResource(id = R.drawable.imagenperfil3),
            contentDescription = "Fondo difuminado",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            // Icono de pantalla
            Image(
                painter = painterResource(id = R.drawable.ic_perfil),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(96.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre del usuario
            Text(
                text = usuario?.nombre ?: "Sin nombre",
                style = TextStyle(
                    fontFamily = YellowPeach,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Mi Perfil",
                        style = TextStyle(fontFamily = YellowPeach, fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row {
                                Text("Email: ", fontWeight = FontWeight.Bold)
                                Text(usuario?.email ?: "Sin correo")
                            }
                            Row {
                                Text("Teléfono: ", fontWeight = FontWeight.Bold)
                                Text(usuario?.telefono ?: "Sin teléfono")
                            }
                        }

                        IconButton(onClick = {
                            showEditDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar perfil",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mis Mascotas",
                style = TextStyle(
                    fontFamily = YellowPeach,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ponemos las cartas de los perfiles de mascotas en una caja con scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa el espacio restante de la pantalla
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mascotas) { mascota ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(Color.White)
                                            .border(
                                                width = 2.dp,
                                                color = Color.White,
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (mascota.fotoUrl.isNotEmpty()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(mascota.fotoUrl),
                                                contentDescription = "Foto de la mascota",
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(id = R.drawable.iconoperro),
                                                contentDescription = "Imagen por defecto perfil mascota",
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(56.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Información de la mascota
                                    Column {
                                        Row {
                                            Text("Nombre: ", fontWeight = FontWeight.Bold)
                                            Text(mascota.nombre)
                                        }
                                        Row {
                                            Text("Raza: ", fontWeight = FontWeight.Bold)
                                            Text(mascota.raza)
                                        }
                                        Row {
                                            Text("Edad: ", fontWeight = FontWeight.Bold)
                                            Text("${calculateAge(mascota.fechaNacimiento.toDate())} años")
                                        }
                                    }
                                }

                                Row {
                                    IconButton(onClick = {
                                        selectedPet = mascota
                                        showEditPetDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar mascota",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(onClick = {
                                        try {
                                            val updatedMascotas = mascotas.filter { it.id != mascota.id }
                                            db.collection("usuarios").document(usuario!!.id)
                                                .update("mascotas", updatedMascotas.map { it.id })
                                                .addOnSuccessListener {
                                                    mascotas = updatedMascotas
                                                }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error al eliminar la mascota: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar mascota",
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CustomButton(
                text = "Añadir Mascota",
                icon = painterResource(id = R.drawable.ic_mas),
                onClick = { showAddPetDialog = true },
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
        }
    }
}

