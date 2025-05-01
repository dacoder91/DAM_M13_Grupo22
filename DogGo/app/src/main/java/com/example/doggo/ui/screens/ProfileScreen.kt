package com.example.doggo.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.doggo.R
import com.example.doggo.models.Mascota
import com.example.doggo.models.Usuario
import com.example.doggo.ui.screens.ui.theme.YellowPeach
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

// Pantalla principal del perfil del usuario. Muestra la información del usuario,
// la lista de mascotas asociadas y permite editar el perfil, añadir o editar mascotas.
@Composable
fun ProfileScreen(navController: NavController) {
    val auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var mascotas by remember { mutableStateOf<List<Mascota>>(emptyList()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddPetDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var selectedPet by remember { mutableStateOf<Mascota?>(null) }


    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo(0)
            }
        } else {
            val usuarioId = currentUser.uid
            db.collection("usuarios").document(usuarioId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val usuarioData = document.toObject(Usuario::class.java)
                        usuario = usuarioData

                        // Cargar las mascotas asociadas al usuario
                        db.collection("mascotas")
                            .whereEqualTo("usuarioId", usuarioId)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val mascotasList = querySnapshot.documents.mapNotNull { document ->
                                    document.toObject(Mascota::class.java)?.copy(id = document.id)
                                }
                                mascotas = mascotasList
                            }
                    }
                }
        }
    }

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

    if (showAddPetDialog && usuario != null) {
        AddPetDialog(
            usuarioId = usuario!!.id,
            onDismiss = { showAddPetDialog = false },
            onSave = { newPet ->
                val petWithUserId = newPet.copy(usuarioId = usuario!!.id)

                // Guardar la mascota en Firestore
                db.collection("mascotas")
                    .add(petWithUserId)
                    .addOnSuccessListener { documentReference ->
                        val updatedMascotas = mascotas + petWithUserId.copy(id = documentReference.id)
                        mascotas = updatedMascotas

                        // Actualizar la lista de mascotas del usuario
                        db.collection("usuarios").document(usuario!!.id)
                            .update("mascotas", updatedMascotas.map { it.id })
                            .addOnSuccessListener {
                                showAddPetDialog = false
                            }
                    }
            }
        )
    }

    if (showEditPetDialog && selectedPet != null) {
        EditPetDialog(
            mascota = selectedPet!!,
            onDismiss = { showEditPetDialog = false },
            onSave = { updatedPet ->
                db.collection("mascotas").document(updatedPet.id)
                    .set(updatedPet)
                    .addOnSuccessListener {
                        mascotas = mascotas.map { if (it.id == updatedPet.id) updatedPet else it }
                        showEditPetDialog = false
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
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("login") {
                    popUpTo(0)
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(width = 100.dp, height = 36.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE91E63),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp) // Botón redondeado
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Salir")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Salir")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            // Icono de pantalla
            Image(
                painter = painterResource(id = R.drawable.perroypersona),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre del usuario
            Text(
                text = usuario?.nombre ?: "Sin nombre",
                style = TextStyle(
                    fontFamily = YellowPeach,
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
                        style = TextStyle(fontFamily = YellowPeach, fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Email: ${usuario?.email ?: "Sin correo"}")
                            Text(text = "Teléfono: ${usuario?.telefono ?: "Sin teléfono"}")
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
                                                painter = painterResource(id = R.drawable.perro),
                                                contentDescription = "Imagen por defecto perfil mascota",
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(56.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Información de la mascota
                                    Column {
                                        Text(text = "Nombre: ${mascota.nombre}")
                                        Text(text = "Raza: ${mascota.raza}")
                                        Text(text = "Edad: ${mascota.edad} años")
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
                                        val updatedMascotas =
                                            mascotas.filter { it.id != mascota.id }
                                        db.collection("usuarios").document(usuario!!.id)
                                            .update("mascotas", updatedMascotas.map { it.id })
                                            .addOnSuccessListener {
                                                mascotas = updatedMascotas
                                            }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar mascota",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showAddPetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir Mascota")
            }
        }
    }
}

// Diálogo para editar la información del perfil del usuario.
// Permite modificar el nombre, email y teléfono del usuario.
@Composable
fun EditProfileDialog(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onSave: (Usuario) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var email by remember { mutableStateOf(usuario.email) }
    var telefono by remember { mutableStateOf(usuario.telefono) }
    val db = FirebaseFirestore.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updatedUsuario = usuario.copy(nombre = nombre, email = email, telefono = telefono)
                db.collection("usuarios").document(usuario.id)
                    .set(updatedUsuario)
                    .addOnSuccessListener {
                        Log.d("FirestoreSuccess", "Perfil actualizado correctamente")
                        onSave(updatedUsuario)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FirestoreError", "Error al actualizar el perfil", exception)
                    }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Diálogo para añadir una nueva mascota. Permite ingresar el nombre, raza,
// edad y URL de la foto de la mascota, y guarda la información en Firestore.
@Composable
fun AddPetDialog(
    usuarioId: String,
    onDismiss: () -> Unit,
    onSave: (Mascota) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Mascota") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = raza,
                    onValueChange = { raza = it },
                    label = { Text("Raza") }
                )
                OutlinedTextField(
                    value = edad,
                    onValueChange = { edad = it },
                    label = { Text("Edad") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = fotoUrl,
                    onValueChange = { fotoUrl = it },
                    label = { Text("URL de la Foto") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (fotoUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(fotoUrl),
                        contentDescription = "Imagen de la mascota",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newPet = Mascota(
                    id = "",
                    nombre = nombre,
                    raza = raza,
                    edad = edad.toIntOrNull() ?: 0,
                    fotoUrl = fotoUrl,
                    usuarioId = usuarioId
                )
                onSave(newPet)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditPetDialog(
    mascota: Mascota,
    onDismiss: () -> Unit,
    onSave: (Mascota) -> Unit
) {
    var nombre by remember { mutableStateOf(mascota.nombre) }
    var raza by remember { mutableStateOf(mascota.raza) }
    var edad by remember { mutableStateOf(mascota.edad.toString()) }
    var fotoUrl by remember { mutableStateOf(mascota.fotoUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Mascota") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = raza,
                    onValueChange = { raza = it },
                    label = { Text("Raza") }
                )
                OutlinedTextField(
                    value = edad,
                    onValueChange = { edad = it },
                    label = { Text("Edad") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = fotoUrl,
                    onValueChange = { fotoUrl = it },
                    label = { Text("URL de la Foto") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (fotoUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(fotoUrl),
                        contentDescription = "Imagen de la mascota",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updatedPet = mascota.copy(
                    nombre = nombre,
                    raza = raza,
                    edad = edad.toIntOrNull() ?: 0,
                    fotoUrl = fotoUrl
                )
                onSave(updatedPet)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}