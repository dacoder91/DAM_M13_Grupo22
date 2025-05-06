package com.example.doggo2.ui.screens


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.doggo2.R
import com.example.doggo2.ui.screens.ui.theme.YellowPeach
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.firestore.Query
import com.example.doggo2.models.MascotaPerdida


@Composable
fun MascotasPerdidasScreen(
    navController: NavController,
    parentNavController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var mascotasPerdidas by remember { mutableStateOf<List<MascotaPerdida>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMascota by remember { mutableStateOf<MascotaPerdida?>(null) }
    var mostrarEncontradas by remember { mutableStateOf(false) }

    LaunchedEffect(mostrarEncontradas) {
        val query = if (mostrarEncontradas) {
            db.collection("mascotasPerdidas")
                .orderBy("fechaPerdida", Query.Direction.DESCENDING)
        } else {
            db.collection("mascotasPerdidas")
                .whereEqualTo("encontrado", false)
                .orderBy("fechaPerdida", Query.Direction.DESCENDING)
        }

        query.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                mascotasPerdidas = snapshot.documents.mapNotNull { document ->
                    document.toObject(MascotaPerdida::class.java)?.copy(id = document.id)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.imagenperroperdido),
            contentDescription = "Fondo difuminado",
            contentScale = ContentScale.Crop,
            alpha = 0.5f,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Fila superior con checkbox y botón de salir
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox para mostrar encontradas
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = mostrarEncontradas,
                        onCheckedChange = { mostrarEncontradas = it }
                    )
                    Text("Mostrar encontradas")
                }

                // Botón Salir
                Button(
                    onClick = {
                        Firebase.auth.signOut()
                        parentNavController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salir")
                }
            }

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Mascotas Perdidas",
                    style = TextStyle(fontFamily = YellowPeach, fontSize = 28.sp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mascotasPerdidas) { mascota ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Indicador de mascota encontrada
                                if (mascota.encontrado) {
                                    Text(
                                        text = "ENCONTRADA",
                                        color = Color.Green,
                                        style = TextStyle(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    if (mascota.fotoUrl.isNotEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(mascota.fotoUrl),
                                            contentDescription = "Foto de la mascota",
                                            modifier = Modifier
                                                .size(192.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.imagenperroperdido),
                                            contentDescription = "Sin imagen",
                                            modifier = Modifier
                                                .size(192.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Nombre: ${mascota.nombreMascota}")
                                Text("Fecha publicación: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(mascota.fechaPerdida.toDate())}")
                                Text("Ubicación: ${mascota.ubicacion.latitude}, ${mascota.ubicacion.longitude}")
                                Text("Contacto: ${mascota.contacto}")

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (mascota.usuarioId == Firebase.auth.currentUser?.uid) {
                                        IconButton(onClick = {
                                            selectedMascota = mascota
                                            showEditDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                        }

                                        IconButton(onClick = {
                                            db.collection("mascotasPerdidas").document(mascota.id).delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                                                }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = mascota.encontrado,
                                                onCheckedChange = { isChecked ->
                                                    db.collection("mascotasPerdidas").document(mascota.id)
                                                        .update("encontrado", isChecked)
                                                        .addOnSuccessListener {
                                                            Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                            )
                                            Text("¿Encontrado?")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showAddDialog = true },
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
                    Text("Añadir anuncio de mascota perdida")
                }
            }
        }
    }

    if (showAddDialog) {
        AddLostPetDialog(
            onDismiss = { showAddDialog = false },
            onSave = { nuevaMascota ->
                db.collection("mascotasPerdidas").add(nuevaMascota)
                    .addOnSuccessListener {
                        Toast.makeText(navController.context, "Mascota añadida", Toast.LENGTH_SHORT).show()
                        showAddDialog = false
                    }
            }
        )
    }

    if (showEditDialog && selectedMascota != null) {
        EditLostPetDialog(
            mascota = selectedMascota!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedMascota ->
                db.collection("mascotasPerdidas").document(updatedMascota.id).set(updatedMascota)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Anuncio actualizado", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLostPetDialog(
    onDismiss: () -> Unit,
    onSave: (MascotaPerdida) -> Unit
) {
    var nombreMascota by remember { mutableStateOf("") }
    var fechaPerdida by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var ubicacion by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }
    var encontrado by remember { mutableStateOf(false) }
    var contacto by remember { mutableStateOf("") }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        fechaPerdida = datePickerState.selectedDateMillis
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho de la pantalla
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val nuevaMascota = MascotaPerdida(
                    usuarioId = Firebase.auth.currentUser?.uid ?: "",
                    nombreMascota = nombreMascota,
                    fechaPerdida = fechaPerdida?.let { Timestamp(it / 1000, 0) } ?: Timestamp.now(),
                    ubicacion = GeoPoint(0.0, 0.0),
                    fotoUrl = fotoUrl,
                    encontrado = encontrado,
                    contacto = contacto
                )
                onSave(nuevaMascota)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Añadir Mascota Perdida") },
        text = {
            Column {
                TextField(
                    value = nombreMascota,
                    onValueChange = { nombreMascota = it },
                    label = { Text("Nombre de la Mascota") }
                )
                Button(onClick = { showDatePicker = true }) {
                    Text("Seleccionar Fecha de Pérdida")
                }
                Text("Fecha seleccionada: ${fechaPerdida?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "No seleccionada"}")
                TextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") }
                )
                TextField(
                    value = fotoUrl,
                    onValueChange = { fotoUrl = it },
                    label = { Text("URL de la Foto") }
                )
                TextField(
                    value = contacto,
                    onValueChange = { contacto = it },
                    label = { Text("Contacto (Teléfono o Email)") }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLostPetDialog(
    mascota: MascotaPerdida,
    onDismiss: () -> Unit,
    onSave: (MascotaPerdida) -> Unit
) {
    var nombreMascota by remember { mutableStateOf(mascota.nombreMascota) }
    var fechaPerdida by remember { mutableStateOf(mascota.fechaPerdida.toDate().time) }
    var showDatePicker by remember { mutableStateOf(false) }
    var ubicacion by remember { mutableStateOf("${mascota.ubicacion.latitude}, ${mascota.ubicacion.longitude}") }
    var fotoUrl by remember { mutableStateOf(mascota.fotoUrl) }
    var contacto by remember { mutableStateOf(mascota.contacto) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fechaPerdida)
        val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        fechaPerdida = datePickerState.selectedDateMillis ?: fechaPerdida
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho de la pantalla
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val updatedMascota = mascota.copy(
                    nombreMascota = nombreMascota,
                    fechaPerdida = Timestamp(fechaPerdida / 1000, 0),
                    ubicacion = GeoPoint(
                        ubicacion.split(",").getOrNull(0)?.toDoubleOrNull() ?: 0.0,
                        ubicacion.split(",").getOrNull(1)?.toDoubleOrNull() ?: 0.0
                    ),
                    fotoUrl = fotoUrl,
                    contacto = contacto
                )
                onSave(updatedMascota)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Editar Mascota Perdida") },
        text = {
            Column {
                TextField(
                    value = nombreMascota,
                    onValueChange = { nombreMascota = it },
                    label = { Text("Nombre de la Mascota") }
                )
                Button(onClick = { showDatePicker = true }) {
                    Text("Seleccionar Fecha de Pérdida")
                }
                Text("Fecha seleccionada: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaPerdida)}")
                TextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación (Latitud, Longitud)") }
                )
                TextField(
                    value = fotoUrl,
                    onValueChange = { fotoUrl = it },
                    label = { Text("URL de la Foto") }
                )
                TextField(
                    value = contacto,
                    onValueChange = { contacto = it },
                    label = { Text("Contacto (Teléfono o Email)") }
                )
            }
        }
    )
}