package com.example.doggo2.ui.screens

import Evento
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.example.doggo2.R
import com.example.doggo2.ui.components.LogoutButton
import com.example.doggo2.ui.components.CustomButton
import com.example.doggo2.ui.screens.ui.theme.YellowPeach
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.style.TextAlign
import com.example.doggo2.controller.getCityFromGeoPoint
import androidx.compose.ui.platform.LocalContext

@Composable
fun EventosScreen(
    navController: NavController,
    parentNavController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = Firebase.auth.currentUser
    val context = LocalContext.current

    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var selectedEvento by remember { mutableStateOf<Evento?>(null) }
    var showMyEventsDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            db.collection("eventos").addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(navController.context, "Error al cargar eventos: ${exception.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    eventos = snapshot.documents.mapNotNull { document ->
                        document.toObject(Evento::class.java)?.copy(id = document.id)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(navController.context, "Error inesperado: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.imageneventos2),
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
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_eventos),
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

            CustomButton(
                text = "Ver Eventos en el Mapa",
                onClick = { showMapDialog = true },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomButton(
                text = "Mis Eventos",
                onClick = { showMyEventsDialog = true },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (eventos.isEmpty()) {
                    item {
                        Text(
                            text = "No hay eventos disponibles",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
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

                                        Row {
                                            Text("Título: ${evento.titulo}", fontSize = 18.sp)
                                        }
                                        Row {
                                            Text(
                                                "Fecha: ${
                                                    SimpleDateFormat(
                                                        "dd/MM/yyyy",
                                                        Locale.getDefault()
                                                    ).format(evento.fecha.toDate())
                                                }"
                                            )
                                        }
                                        Row {
                                            Text(
                                                "Ubicación: ${
                                                    getCityFromGeoPoint(
                                                        context,
                                                        evento.ubicacion
                                                    )
                                                }"
                                            )
                                        }
                                        Row {
                                            Text("Participantes: ${evento.participantes.size}/${evento.maxParticipantes}")
                                        }

                                        if (evento.creadorId == currentUser?.uid) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(onClick = {
                                                    selectedEvento = evento
                                                    showEditDialog = true
                                                }) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Editar",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                IconButton(onClick = {
                                                    db.collection("eventos").document(evento.id)
                                                        .delete()
                                                        .addOnSuccessListener {
                                                            Toast.makeText(
                                                                navController.context,
                                                                "Evento eliminado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                }) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Eliminar",
                                                        tint = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Botón Unirse
                                        CustomButton(
                                            text = when {
                                                evento.participantes.contains(currentUser?.uid) -> "Unido ✓"
                                                evento.participantes.size >= evento.maxParticipantes -> "Completo"
                                                else -> "Unirse"
                                            },
                                            onClick = {
                                                try {
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
                                                            val updatedParticipantes =
                                                                evento.participantes + currentUser!!.uid
                                                            db.collection("eventos")
                                                                .document(evento.id)
                                                                .update(
                                                                    "participantes",
                                                                    updatedParticipantes
                                                                )
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(
                                                                        navController.context,
                                                                        "¡Te has unido al evento!",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Toast.makeText(
                                                                        navController.context,
                                                                        "Error al unirse: ${e.message}",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        navController.context,
                                                        "Error inesperado: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        )

                                        // Botón +Info
                                        CustomButton(
                                            text = "+Info",
                                            onClick = {
                                                selectedEvento = evento
                                                showEditDialog = false
                                                showInfoDialog = true
                                            }
                                        )

                                        // Botón Chat
                                        CustomButton(
                                            text = "Chat",
                                            onClick = {
                                                if (evento.participantes.contains(currentUser?.uid)) {
                                                    selectedEvento = evento
                                                    showChatDialog = true
                                                } else {
                                                    Toast.makeText(
                                                        navController.context,
                                                        "Chat solo disponible para usuarios unidos al evento",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
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
                text = "Añadir Evento",
                icon = painterResource(id = R.drawable.ic_mas),
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
        }
    }

    if (showMapDialog) {
        EventMapDialog(
            eventos = eventos,
            onDismiss = { showMapDialog = false }
        )
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onSave = { nuevoEvento ->
                try {
                    db.collection("eventos").add(nuevoEvento)
                        .addOnSuccessListener {
                            Toast.makeText(navController.context, "Evento añadido", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(navController.context, "Error al añadir evento: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(navController.context, "Error inesperado: ${e.message}", Toast.LENGTH_SHORT).show()
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

    if (showChatDialog && selectedEvento != null) {
        ChatDialog(
            eventoId = selectedEvento!!.id,
            currentUserId = currentUser?.uid ?: "",
            onDismiss = { showChatDialog = false }
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
                                            Text("Ubicación: ${getCityFromGeoPoint(context, evento.ubicacion)}")
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
                                                            showMyEventsDialog = false
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
                                                    containerColor = Color(0xFF2196F3),
                                                    contentColor = Color.White
                                                )
                                            ) {
                                                Text("+Info")
                                            }
                                        }
                                    }

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

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                selectedEvento = evento
                                                showMyEventsDialog = false
                                                showChatDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text("Chat")
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