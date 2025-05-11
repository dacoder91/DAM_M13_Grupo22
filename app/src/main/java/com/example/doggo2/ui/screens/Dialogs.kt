package com.example.doggo2.ui.screens

import Evento
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.example.doggo.models.Mascota
import com.example.doggo2.controller.calculateAge
import com.example.doggo2.controller.enviarMensaje
import com.example.doggo2.controller.getCityFromGeoPoint
import com.example.doggo2.controller.validarCamposEvento
import com.example.doggo2.models.MascotaPerdida
import com.example.doggo2.models.Usuario
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.doggo2.controller.createMapView

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
                val updatedUsuario =
                    usuario.copy(nombre = nombre, email = email, telefono = telefono)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetDialog(
    usuarioId: String,
    onDismiss: () -> Unit,
    onSave: (Mascota) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var fotoUrl by remember { mutableStateOf("") }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaNacimiento = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
                Button(onClick = { showDatePicker = true }) {
                    Text("Seleccionar Fecha de Nacimiento")
                }
                Text(
                    text = "Fecha seleccionada: ${
                        fechaNacimiento?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "No seleccionada"
                    }"
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
                if (fechaNacimiento == null) {
                    return@TextButton
                }
                val newPet = Mascota(
                    id = "",
                    nombre = nombre,
                    raza = raza,
                    fotoUrl = fotoUrl,
                    usuarioId = usuarioId,
                    fechaNacimiento = Timestamp(Date(fechaNacimiento!!))
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

// Dialogo para editar la información de una mascota existente.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetDialog(
    mascota: Mascota,
    onDismiss: () -> Unit,
    onSave: (Mascota) -> Unit
) {
    var nombre by remember { mutableStateOf(mascota.nombre) }
    var raza by remember { mutableStateOf(mascota.raza) }
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
                // Mostrar fecha de nacimiento (solo lectura)
                Text(
                    "Fecha de Nacimiento: ${
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(mascota.fechaNacimiento.toDate())
                    }"
                )
                Text("Edad: ${calculateAge(mascota.fechaNacimiento.toDate())} años")
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

// Diálogo para registrar una nueva cuenta. Permite ingresar nombre de usuario, email y contraseña,
@Composable
fun RegisterDialog(onDismiss: () -> Unit, onRegister: (String, String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Cuenta") },
        text = {
            Column {
                // Campo de nombre de usuario
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox para aceptar condiciones de uso
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = { acceptTerms = it }
                    )
                    Text("Aceptar condiciones de uso")
                }

                // Texto para ver condiciones
                TextButton(onClick = { showTermsDialog = true }) {
                    Text("Ver condiciones")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Primero valida si los campos están completos
                if (username.isBlank() || email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Debe rellenar todos los campos", Toast.LENGTH_SHORT)
                        .show()
                    return@TextButton
                }

                // Luego valida si las condiciones de uso están aceptadas
                if (!acceptTerms) {
                    Toast.makeText(
                        context,
                        "Debe aceptar las condiciones de uso",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@TextButton
                }

                // Verificar si el email ya existe
                val db = FirebaseFirestore.getInstance()
                db.collection("usuarios")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { emailDocuments ->
                        if (!emailDocuments.isEmpty) {
                            Toast.makeText(
                                context,
                                "El email ya ha sido registrado",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Verificar si el nombre de usuario ya existe
                            db.collection("usuarios")
                                .whereEqualTo("nombre", username)
                                .get()
                                .addOnSuccessListener { usernameDocuments ->
                                    if (!usernameDocuments.isEmpty) {
                                        Toast.makeText(
                                            context,
                                            "El nombre de usuario no está disponible",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        onRegister(username, email, password)
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Error al verificar el nombre de usuario, el nombre ya esta en uso",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Error al verificar el email. Mail ya registrado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // Diálogo para mostrar las condiciones de uso
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Condiciones de Uso") },
            text = {
                Text(
                    "Estas son las condiciones de uso genéricas de la aplicación. " +
                            "Al registrarte, aceptas cumplir con las políticas y términos establecidos."
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

//Dialogo para añadir mascota perdida
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLostPetDialog(
    onDismiss: () -> Unit,
    onSave: (MascotaPerdida) -> Unit
) {
    var nombreMascota by remember { mutableStateOf("") }
    var fechaPerdida by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var ubicacion by remember {
        mutableStateOf(
            GeoPoint(
                41.3879,
                2.16992
            )
        )
    } // Posición inicial: Barcelona
    var fotoUrl by remember { mutableStateOf("") }
    var encontrado by remember { mutableStateOf(false) }
    var contacto by remember { mutableStateOf("") }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled =
            remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

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
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val nuevaMascota = MascotaPerdida(
                    usuarioId = Firebase.auth.currentUser?.uid ?: "",
                    nombreMascota = nombreMascota,
                    fechaPerdida = fechaPerdida?.let { Timestamp(it / 1000, 0) } ?: Timestamp.now(),
                    ubicacion = ubicacion,
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
                Text(
                    "Fecha seleccionada: ${
                        fechaPerdida?.let {
                            SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(it)
                        } ?: "No seleccionada"
                    }")
                Text("Ubicación inicial: ${ubicacion.latitude}, ${ubicacion.longitude}")
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

//Dialogo para editar mascota perdida
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLostPetDialog(
    mascota: MascotaPerdida,
    onDismiss: () -> Unit,
    onSave: (MascotaPerdida) -> Unit
) {
    var nombreMascota by remember { mutableStateOf(mascota.nombreMascota) }
    var fechaPerdida by remember { mutableStateOf(mascota.fechaPerdida.toDate().time) }
    var fotoUrl by remember { mutableStateOf(mascota.fotoUrl) }
    var contacto by remember { mutableStateOf(mascota.contacto) }
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Mascota Perdida") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombreMascota,
                    onValueChange = { nombreMascota = it },
                    label = { Text("Nombre de la Mascota") }
                )
                OutlinedTextField(
                    value = contacto,
                    onValueChange = { contacto = it },
                    label = { Text("Contacto") }
                )
                OutlinedTextField(
                    value = fotoUrl,
                    onValueChange = { fotoUrl = it },
                    label = { Text("URL de la Foto") }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Botones de editar y eliminar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        db.collection("mascotasPerdidas").document(mascota.id).delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }

                    IconButton(onClick = {
                        val updatedMascota = mascota.copy(
                            nombreMascota = nombreMascota,
                            contacto = contacto,
                            fotoUrl = fotoUrl
                        )
                        onSave(updatedMascota)
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Guardar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

//Dialogo para seleccionar ubicacion en el mapa
@Composable
fun MapDialog2(
    initialLocation: GeoPoint,
    onDismiss: () -> Unit,
    onLocationSelected: (GeoPoint) -> Unit
) {
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (selectedLocation == null) {
                    Toast.makeText(context, "Por favor selecciona una ubicación", Toast.LENGTH_SHORT).show()
                } else {
                    onLocationSelected(selectedLocation!!)
                }
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Atrás")
            }
        },
        title = { Text("Seleccionar Ubicación") },
        text = {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        onCreate(null)
                        onResume() // Asegúrate de llamar a onResume
                        getMapAsync { googleMap ->
                            val initialLatLng = LatLng(initialLocation.latitude, initialLocation.longitude)
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 12f))
                            googleMap.setOnMapClickListener { latLng ->
                                selectedLocation = GeoPoint(latLng.latitude, latLng.longitude)
                                googleMap.clear()
                                googleMap.addMarker(MarkerOptions().position(latLng))
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                update = { mapView ->
                    mapView.onResume() // Asegúrate de que el mapa se actualice correctamente
                }
            )
        }
    )
}

//Dialogo para añadir evento
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSave: (Evento) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember {
        mutableStateOf(
            GeoPoint(
                41.3879,
                2.16992
            )
        )
    }
    var fecha by remember { mutableStateOf<Long?>(null) }
    var tipo by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showMapDialog) {
        MapDialog2(
            initialLocation = ubicacion, // Ubicación inicial
            onDismiss = { showMapDialog = false },
            onLocationSelected = { selectedLocation ->
                ubicacion = selectedLocation // Actualiza la ubicación seleccionada
                showMapDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                try {
                    if (validarCamposEvento(titulo, descripcion, ubicacion, fecha, tipo, context)) {
                        val nuevoEvento = Evento(
                            titulo = titulo,
                            descripcion = descripcion,
                            ubicacion = ubicacion,
                            fecha = Timestamp(Date(fecha ?: System.currentTimeMillis())),
                            maxParticipantes = 15,
                            tipo = tipo,
                            creadorId = com.google.firebase.ktx.Firebase.auth.currentUser?.uid ?: ""
                        )
                        onSave(nuevoEvento)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al guardar el evento: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
                    label = { Text("Descripción") },
                    modifier = Modifier.clickable { showDescriptionDialog = true }
                )
                Button(onClick = { showDatePicker = true }) {
                    Text("Seleccionar Fecha")
                }
                Text(
                    "Fecha seleccionada: ${
                        fecha?.let {
                            SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(it)
                        } ?: "No seleccionada"
                    }")
                TextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo de Evento") }
                )
                Button(onClick = { showMapDialog = true }) {
                    Text("Seleccionar Ubicación en el Mapa")
                }
                Text("Ubicación seleccionada: ${ubicacion.latitude}, ${ubicacion.longitude}")
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


// Diálogo para editar un evento existente. Permite modificar el título, descripción, ubicación, fecha y tipo del evento.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventDialog(
    evento: Evento,
    onDismiss: () -> Unit,
    onSave: (Evento) -> Unit
) {
    var titulo by remember { mutableStateOf(evento.titulo) }
    var descripcion by remember { mutableStateOf(evento.descripcion) }
    var ubicacion by remember { mutableStateOf(evento.ubicacion) }
    var fecha by remember { mutableStateOf<Long?>(evento.fecha.toDate().time) }
    var tipo by remember { mutableStateOf(evento.tipo) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showMapDialog) {
        MapDialog2(
            initialLocation = ubicacion,
            onDismiss = { showMapDialog = false },
            onLocationSelected = { selectedLocation ->
                ubicacion = selectedLocation
                showMapDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                try {
                    if (validarCamposEvento(titulo, descripcion, ubicacion, fecha, tipo, context)) {
                        val nuevoEvento = Evento(
                            titulo = titulo,
                            descripcion = descripcion,
                            ubicacion = ubicacion,
                            fecha = Timestamp(Date(fecha ?: System.currentTimeMillis())),
                            maxParticipantes = 15,
                            tipo = tipo,
                            creadorId = com.google.firebase.ktx.Firebase.auth.currentUser?.uid ?: ""
                        )
                        onSave(nuevoEvento)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al editar el evento: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
                    label = { Text("Descripción") },
                    modifier = Modifier.clickable { showDescriptionDialog = true }
                )
                Button(onClick = { showDatePicker = true }) {
                    Text("Seleccionar Fecha")
                }
                Text(
                    "Fecha seleccionada: ${
                        fecha?.let {
                            SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(it)
                        } ?: "No seleccionada"
                    }")
                TextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo de Evento") }
                )
                Button(onClick = { showMapDialog = true }) {
                    Text("Seleccionar Ubicación en el Mapa")
                }
                Text("Ubicación seleccionada: ${ubicacion.latitude}, ${ubicacion.longitude}")
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


// Diálogo para editar la descripción de un evento. Permite modificar el texto de la descripción.
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

// Diálogo para mostrar la información de un evento. Muestra el título, descripción, ubicación, fecha y tipo del evento.
@Composable
fun InfoEventDialog(
    evento: Evento,
    onDismiss: () -> Unit
) {
    var showLocationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                Text("Ubicación: ${getCityFromGeoPoint(context, evento.ubicacion)}")
                Button(
                    onClick = { showLocationDialog = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Ver Ubicación")
                }
                Text(
                    "Fecha: ${
                        SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(evento.fecha.toDate())
                    }"
                )
                Text("Participantes: ${evento.participantes.size}/${evento.maxParticipantes}")
                Text("Tipo: ${evento.tipo}")
            }
        }
    )

    if (showLocationDialog) {
        LocationDialog(
            location = evento.ubicacion,
            onDismiss = { showLocationDialog = false }
        )
    }
}

// Diálogo para mostrar la ubicación de un evento en un mapa. Muestra la latitud y longitud del evento y permite ver su ubicación en un mapa.
@Composable
fun LocationDialog(
    location: GeoPoint,
    onDismiss: () -> Unit
) {
    var mapView: MapView? = null

    AlertDialog(
        onDismissRequest = {
            mapView?.onPause()
            mapView?.onDestroy()
            onDismiss()
        },
        confirmButton = {
            TextButton(onClick = {
                mapView?.onPause()
                mapView?.onDestroy()
                onDismiss()
            }) {
                Text("Atrás")
            }
        },
        title = { Text("Ubicación del Evento") },
        text = {
            Column {
                Text("Latitud: ${location.latitude}")
                Text("Longitud: ${location.longitude}")
                Spacer(modifier = Modifier.height(16.dp))
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            onCreate(null)
                            onResume()
                            getMapAsync { googleMap ->
                                val eventLocation = LatLng(location.latitude, location.longitude)
                                googleMap.uiSettings.isZoomControlsEnabled = true
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(eventLocation, 15f)
                                )
                                googleMap.addMarker(
                                    MarkerOptions()
                                        .position(eventLocation)
                                        .title("Ubicación del Evento")
                                )
                            }
                            mapView = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    update = { it.onResume() }
                )
            }
        }
    )
}

//Diálogo para mostrar un mapa con los eventos disponibles.
// Permite al usuario ver la ubicación de los eventos en un mapa y seleccionar uno para obtener más información.
@Composable
fun EventMapDialog(
    eventos: List<Evento>,
    onDismiss: () -> Unit
) {
    var mapView: MapView? = null
    var selectedMarkerTitle by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                mapView?.onPause()
                mapView?.onDestroy()
                onDismiss()
            }) {
                Text("Cerrar")
            }
        },
        title = { Text("Eventos en el Mapa") },
        text = {
            Column {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            onCreate(null)
                            getMapAsync { googleMap ->
                                googleMap.uiSettings.isZoomControlsEnabled = true

                                eventos.forEach { evento ->
                                    val location = LatLng(
                                        evento.ubicacion.latitude,
                                        evento.ubicacion.longitude
                                    )
                                    val markerTitle = evento.titulo.ifEmpty { "Evento sin título" }
                                    val marker = googleMap.addMarker(
                                        MarkerOptions()
                                            .position(location)
                                            .title(markerTitle)
                                    )

                                    googleMap.setOnMarkerClickListener { clickedMarker ->
                                        if (clickedMarker == marker) {
                                            selectedMarkerTitle = markerTitle
                                        } else {
                                            selectedMarkerTitle = null
                                        }
                                        false
                                    }
                                }

                                if (eventos.isNotEmpty()) {
                                    val firstLocation = LatLng(
                                        eventos[0].ubicacion.latitude,
                                        eventos[0].ubicacion.longitude
                                    )
                                    googleMap.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            firstLocation,
                                            10f
                                        )
                                    )
                                }
                            }
                            mapView = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    update = { it.onResume() }
                )
                selectedMarkerTitle?.let { title ->
                    Text(
                        text = "Evento seleccionado: $title",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}


//Diálogo para mostrar un chat en tiempo real. Permite a los usuarios enviar y recibir mensajes
// en un evento específico.
@Composable
fun ChatDialog(
    eventoId: String,
    currentUserId: String,
    onDismiss: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val mensajes = remember { mutableStateListOf<Map<String, Any>>() }
    val usuarios = remember { mutableStateMapOf<String, String>() } // Mapa de senderId a nombre
    var textoMensaje by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) } // Para manejar errores

    // Escuchar mensajes en tiempo real
    LaunchedEffect(eventoId) {
        db.collection("Eventos")
            .document(eventoId)
            .collection("Chats")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorMessage = "Error al cargar mensajes: ${e.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    mensajes.clear()
                    mensajes.addAll(snapshot.documents.mapNotNull { it.data })

                    // Cargar nombres de usuarios
                    snapshot.documents.forEach { document ->
                        val senderId = document.getString("senderId") ?: return@forEach
                        if (!usuarios.containsKey(senderId)) {
                            db.collection("usuarios").document(senderId).get()
                                .addOnSuccessListener { userDoc ->
                                    val nombre = userDoc.getString("nombre") ?: "Desconocido"
                                    usuarios[senderId] = nombre
                                }
                                .addOnFailureListener {
                                    errorMessage = "Error al cargar usuario: ${it.message}"
                                }
                        }
                    }
                }
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chat del Evento") },
        text = {
            Column {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(mensajes) { mensaje ->
                        val senderId = mensaje["senderId"] as? String ?: "Desconocido"
                        val nombre = usuarios[senderId] ?: "Cargando..."
                        val texto = mensaje["text"] as? String ?: ""
                        Text("$nombre: $texto")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    TextField(
                        value = textoMensaje,
                        onValueChange = { textoMensaje = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Escribe un mensaje") }
                    )
                    Button(onClick = {
                        if (textoMensaje.isNotBlank()) {
                            try {
                                enviarMensaje(eventoId, currentUserId, textoMensaje)
                                textoMensaje = ""
                            } catch (e: Exception) {
                                errorMessage = "Error al enviar mensaje: ${e.message}"
                            }
                        }
                    }) {
                        Text("Enviar")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )

    // Mostrar Snackbar para errores
    errorMessage?.let { message ->
        Snackbar(
            action = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("Cerrar")
                }
            }
        ) {
            Text(message)
        }
    }
}