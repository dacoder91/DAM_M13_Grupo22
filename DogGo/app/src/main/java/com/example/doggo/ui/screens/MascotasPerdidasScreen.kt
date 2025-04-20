package com.example.doggo.ui.screens

import MascotaPerdida
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MascotasPerdidasScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var mascotasPerdidas by remember { mutableStateOf<List<MascotaPerdida>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMascota by remember { mutableStateOf<MascotaPerdida?>(null) }

    // Cargar mascotas perdidas
    LaunchedEffect(Unit) {
        db.collection("mascotasPerdidas")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    mascotasPerdidas = snapshot.documents.mapNotNull { document ->
                        document.toObject(MascotaPerdida::class.java)?.copy(id = document.id)
                    }
                }
            }
    }

    if (showAddDialog) {
        AddLostPetDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newMascota ->
                db.collection("mascotasPerdidas").add(newMascota)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Mascota agregada", Toast.LENGTH_SHORT).show()
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
                db.collection("mascotasPerdidas").document(updatedMascota.id)
                    .set(updatedMascota)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Mascota actualizada", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mascotas Perdidas", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showAddDialog = true }) {
            Text("Agregar Mascota Perdida")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(mascotasPerdidas) { mascota ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Imagen centrada
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (mascota.fotoUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(mascota.fotoUrl),
                                    contentDescription = "Foto de la mascota",
                                    modifier = Modifier
                                        .size(192.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Datos de la mascota
                        Column {
                            Text("Nombre: ${mascota.nombreMascota}")
                            Text("Fecha publicación: ${mascota.fechaPerdida.toDate()}")
                            Text("Ubicación: ${mascota.ubicacion.latitude}, ${mascota.ubicacion.longitude}")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones centrados
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(onClick = {
                                selectedMascota = mascota
                                showEditDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar mascota",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(onClick = {
                                if (!mascota.id.isNullOrEmpty()) {
                                    db.collection("mascotasPerdidas").document(mascota.id).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(context, "ID de la mascota no válido", Toast.LENGTH_SHORT).show()
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
}

@Composable
fun AddLostPetDialog(
    onDismiss: () -> Unit,
    onSave: (MascotaPerdida) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }
    var fechaPerdida by remember { mutableStateOf(Timestamp.now()) }
    var encontrado by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Mascota Perdida") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la mascota") }
                )
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación (latitud,longitud)") }
                )
                OutlinedTextField(
                    value = fotoUrl,
                    onValueChange = { fotoUrl = it },
                    label = { Text("URL de la Foto") }
                )
                CustomDatePickerField(
                    label = "Fecha de Pérdida",
                    selectedDate = fechaPerdida,
                    onDateSelected = { selectedDate -> fechaPerdida = selectedDate }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = encontrado,
                        onCheckedChange = { encontrado = it }
                    )
                    Text("¿Encontrado?")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val coords = ubicacion.split(",").mapNotNull { it.toDoubleOrNull() }
                if (coords.size == 2) {
                    val nuevaMascota = MascotaPerdida(
                        id = "",
                        nombreMascota = nombre,
                        fechaPerdida = fechaPerdida,
                        ubicacion = GeoPoint(coords[0], coords[1]),
                        fotoUrl = fotoUrl,
                        encontrado = encontrado
                    )
                    onSave(nuevaMascota)
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

@Composable
fun EditLostPetDialog(
    mascota: MascotaPerdida,
    onDismiss: () -> Unit,
    onSave: (MascotaPerdida) -> Unit
) {
    var nombre by remember { mutableStateOf(mascota.nombreMascota) }
    var ubicacion by remember { mutableStateOf("${mascota.ubicacion.latitude},${mascota.ubicacion.longitude}") }
    var fotoUrl by remember { mutableStateOf(mascota.fotoUrl) }
    var fechaPerdida by remember { mutableStateOf(mascota.fechaPerdida) }
    var encontrado by remember { mutableStateOf(mascota.encontrado) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Mascota Perdida") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la mascota") }
                )
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación (latitud,longitud)") }
                )
                OutlinedTextField(
                    value = fotoUrl,
                    onValueChange = { fotoUrl = it },
                    label = { Text("URL de la Foto") }
                )
                CustomDatePickerField(
                    label = "Fecha de Pérdida",
                    selectedDate = fechaPerdida,
                    onDateSelected = { selectedDate -> fechaPerdida = selectedDate }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = encontrado,
                        onCheckedChange = { encontrado = it }
                    )
                    Text("¿Encontrado?")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val coords = ubicacion.split(",").mapNotNull { it.toDoubleOrNull() }
                if (coords.size == 2) {
                    val updatedMascota = mascota.copy(
                        nombreMascota = nombre,
                        fechaPerdida = fechaPerdida,
                        ubicacion = GeoPoint(coords[0], coords[1]),
                        fotoUrl = fotoUrl,
                        encontrado = encontrado
                    )
                    onSave(updatedMascota)
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

@Composable
fun CustomDatePickerField(
    label: String,
    selectedDate: Timestamp,
    onDateSelected: (Timestamp) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var dateInput by remember { mutableStateOf(dateFormatter.format(selectedDate.toDate())) }
    var isError by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = dateInput,
            onValueChange = { dateInput = it },
            label = { Text(label) },
            singleLine = true,
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )

        if (isError) {
            Text(
                text = "Formato de fecha inválido. Use dd/MM/yyyy",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(onClick = {
            try {
                val parsedDate = dateFormatter.parse(dateInput)
                if (parsedDate != null) {
                    onDateSelected(Timestamp(parsedDate))
                    isError = false
                }
            } catch (e: Exception) {
                isError = true
                Toast.makeText(context, "Formato de fecha inválido. Use dd/MM/yyyy", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Aceptar")
        }
    }
}