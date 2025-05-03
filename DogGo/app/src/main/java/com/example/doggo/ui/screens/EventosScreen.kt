package com.example.doggo.ui.screens

import Evento
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import com.example.doggo.R
import com.example.doggo.ui.screens.ui.theme.YellowPeach
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun EventosScreen(
    navController: NavController,
    parentNavController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = Firebase.auth.currentUser
    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var selectedEvento by remember { mutableStateOf<Evento?>(null) }
    var showMyEventsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("eventos").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                eventos = snapshot.documents.mapNotNull { document ->
                    document.toObject(Evento::class.java)?.copy(id = document.id)
                }
            }
        }
    }

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
                Firebase.auth.signOut()
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
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "")
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

            Button(
                onClick = { showMyEventsDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Mis Eventos")
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(eventos) { evento ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Título: ${evento.titulo}", fontSize = 18.sp)
                                    Text("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(evento.fecha.toDate())}")
                                    Text("Ubicación: ${evento.ubicacion.latitude}, ${evento.ubicacion.longitude}")
                                    Text("Participantes: ${evento.participantes.size}/${evento.maxParticipantes}")
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Button(
                                        onClick = {
                                            when {
                                                evento.participantes.contains(currentUser?.uid) -> {
                                                    Toast.makeText(
                                                        navController.context,
                                                        "Ya estás unido a este evento",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                evento.participantes.size >= evento.maxParticipantes -> {
                                                    Toast.makeText(
                                                        navController.context,
                                                        "Evento completo",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                else -> {
                                                    val updatedParticipantes = evento.participantes + currentUser!!.uid
                                                    db.collection("eventos").document(evento.id)
                                                        .update("participantes", updatedParticipantes)
                                                        .addOnSuccessListener {
                                                            Toast.makeText(
                                                                navController.context,
                                                                "¡Te has unido al evento!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (evento.participantes.contains(currentUser?.uid)) {
                                                Color.LightGray // Color cuando ya está unido
                                            } else {
                                                Color(0xFF4CAF50) // Verde normal
                                            },
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Text(
                                            text = if (evento.participantes.contains(currentUser?.uid)) {
                                                "Unido ✓"
                                            } else if (evento.participantes.size >= evento.maxParticipantes) {
                                                "Completo"
                                            } else {
                                                "Unirse"
                                            },
                                            fontFamily = YellowPeach,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            selectedEvento = evento
                                            showEditDialog = false // Asegurarse de que no se muestre el diálogo de edición
                                            showInfoDialog = true // Mostrar el nuevo diálogo informativo
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2196F3), // Azul
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = "+Info",
                                            fontFamily = YellowPeach,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            if (evento.creadorId == currentUser?.uid) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        selectedEvento = evento
                                        showEditDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    IconButton(onClick = {
                                        db.collection("eventos").document(evento.id).delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    navController.context,
                                                    "Evento eliminado",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
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
                Text("Añadir evento")
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onSave = { nuevoEvento ->
                db.collection("eventos").add(nuevoEvento)
                    .addOnSuccessListener {
                        Toast.makeText(navController.context, "Evento añadido", Toast.LENGTH_SHORT).show()
                        showAddDialog = false
                    }
            }
        )
    }

    if (showEditDialog && selectedEvento != null) {
        EditEventDialog(
            evento = selectedEvento!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedEvento ->
                db.collection("eventos").document(updatedEvento.id).set(updatedEvento)
                    .addOnSuccessListener {
                        Toast.makeText(navController.context, "Evento actualizado", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    }
            }
        )
    }

    if (showInfoDialog && selectedEvento != null) {
        InfoEventDialog(
            evento = selectedEvento!!,
            onDismiss = { showInfoDialog = false }
        )
    }

    if (showMyEventsDialog) {
        AlertDialog(
            onDismissRequest = { showMyEventsDialog = false },
            modifier = Modifier.fillMaxHeight(0.8f),
            title = {
                Text(
                    text = "Mis Eventos",
                    fontFamily = YellowPeach,
                    fontSize = 24.sp
                )
            },
            text = {
                val myEvents = eventos.filter { it.participantes.contains(currentUser?.uid) }

                if (myEvents.isEmpty()) {
                    Text("No estás apuntado a ningún evento")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(myEvents) { evento ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Título: ${evento.titulo}", fontSize = 18.sp)
                                            Text("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(evento.fecha.toDate())}")
                                            Text("Ubicación: ${evento.ubicacion.latitude}, ${evento.ubicacion.longitude}")
                                            Text("Participantes: ${evento.participantes.size}/${evento.maxParticipantes}")
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Button(
                                                onClick = {
                                                    val updatedParticipantes = evento.participantes - currentUser!!.uid
                                                    db.collection("eventos").document(evento.id)
                                                        .update("participantes", updatedParticipantes)
                                                        .addOnSuccessListener {
                                                            Toast.makeText(
                                                                navController.context,
                                                                "Te has salido del evento",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            showMyEventsDialog = false // Solo cierra el diálogo
                                                        }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFF44336),
                                                    contentColor = Color.White
                                                ),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            ) {
                                                Text("Borrarse")
                                            }

                                            Button(
                                                onClick = {
                                                    selectedEvento = evento
                                                    showMyEventsDialog = false
                                                    showInfoDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF2196F3), // Azul
                                                    contentColor = Color.White
                                                )
                                            ) {
                                                Text("+Info")
                                            }
                                        }
                                    }

                                    if (evento.creadorId == currentUser?.uid) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = {
                                                selectedEvento = evento
                                                showMyEventsDialog = false
                                                showEditDialog = true
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                                            }

                                            IconButton(onClick = {
                                                db.collection("eventos").document(evento.id).delete()
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            navController.context,
                                                            "Evento eliminado",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMyEventsDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSave: (Evento) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var tipo by remember { mutableStateOf("") }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fecha = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val nuevoEvento = Evento(
                    titulo = titulo,
                    descripcion = descripcion,
                    ubicacion = GeoPoint(0.0, 0.0),
                    fecha = Timestamp(Date(fecha ?: System.currentTimeMillis())),
                    maxParticipantes = 15,
                    tipo = tipo,
                    creadorId = Firebase.auth.currentUser?.uid ?: ""
                )
                onSave(nuevoEvento)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Añadir Evento") },
        text = {
            Column {
                TextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") }
                )
                TextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") }
                )
                TextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") }
                )
                Button(onClick = { showDatePicker = true }) {
                    Text("Seleccionar Fecha")
                }
                Text("Fecha seleccionada: ${fecha?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "No seleccionada"}")
                TextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo de Evento") }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventDialog(
    evento: Evento,
    onDismiss: () -> Unit,
    onSave: (Evento) -> Unit
) {
    var titulo by remember { mutableStateOf(evento.titulo) }
    var descripcion by remember { mutableStateOf(evento.descripcion) }
    var ubicacion by remember { mutableStateOf("${evento.ubicacion.latitude}, ${evento.ubicacion.longitude}") }
    var fecha by remember { mutableStateOf<Long?>(evento.fecha.toDate().time) }
    var showDatePicker by remember { mutableStateOf(false) }
    var tipo by remember { mutableStateOf(evento.tipo) }
    var showDescriptionDialog by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fecha)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fecha = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val updatedEvento = evento.copy(
                    titulo = titulo,
                    descripcion = descripcion,
                    ubicacion = GeoPoint(0.0, 0.0),
                    fecha = Timestamp(Date(fecha ?: System.currentTimeMillis())),
                    tipo = tipo
                )
                onSave(updatedEvento)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Editar Evento") },
        text = {
            Column {
                TextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") }
                )
                TextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") }
                )
                TextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") }
                )
                Button(onClick = { showDatePicker = true }) {
                    Text("Seleccionar Fecha")
                }
                Text("Fecha seleccionada: ${fecha?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "No seleccionada"}")
                TextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo de Evento") }
                )
            }
        }
    )

    if (showDescriptionDialog) {
        LongDescriptionDialog(
            initialText = descripcion,
            onDismiss = { showDescriptionDialog = false },
            onSave = {
                descripcion = it
                showDescriptionDialog = false
            }
        )
    }
}

@Composable
fun LongDescriptionDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Editar Descripción") },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.height(200.dp),
                label = { Text("Descripción") },
                maxLines = 10
            )
        }
    )
}

@Composable
fun InfoEventDialog(
    evento: Evento,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        title = { Text("Información del Evento") },
        text = {
            Column {
                Text("Título: ${evento.titulo}", fontWeight = FontWeight.Bold)
                Text("Descripción: ${evento.descripcion}")
                Text("Ubicación: ${evento.ubicacion.latitude}, ${evento.ubicacion.longitude}")
                Text("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(evento.fecha.toDate())}")
                Text("Participantes: ${evento.participantes.size}/${evento.maxParticipantes}")
                Text("Tipo: ${evento.tipo}")
            }
        }
    )
}
