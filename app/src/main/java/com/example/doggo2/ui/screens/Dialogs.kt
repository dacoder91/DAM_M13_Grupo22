package com.example.doggo2.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.doggo.models.Mascota
import com.example.doggo2.controller.calculateAge
import com.example.doggo2.models.Usuario
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

// Modifica el EditPetDialog:
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