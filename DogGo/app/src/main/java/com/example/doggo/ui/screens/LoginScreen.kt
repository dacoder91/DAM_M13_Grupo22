package com.example.doggo.ui.screens

import android.widget.Toast
import com.example.doggo.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(navController: NavController) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    // Verificación de sesión activa
    if (currentUser != null) {
        navController.navigate("main") {
            popUpTo("login") { inclusive = true }
        }
        return
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.imagenfondologin),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        // Contenido por encima
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\uD83D\uDC3E DogGo",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = errorMessage != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                isError = errorMessage != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Iniciar Sesión
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Por favor completa todos los campos"
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        try {
                            auth.signInWithEmailAndPassword(email, password).await()
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        } catch (e: FirebaseAuthInvalidUserException) {
                            errorMessage = "Usuario no registrado"
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Credenciales incorrectas"
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.large,
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Iniciar sesión")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto para abrir el diálogo de registro
            TextButton(
                onClick = { showRegisterDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("¿No tienes cuenta? ¡Regístrate!", style = MaterialTheme.typography.bodyMedium)
            }

            // Loading y errores
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }

    // Diálogo para registrar una nueva cuenta
    if (showRegisterDialog) {
        RegisterDialog(
            onDismiss = { showRegisterDialog = false },
            onRegister = { email, password ->
                scope.launch {
                    isLoading = true
                    try {
                        auth.createUserWithEmailAndPassword(email, password).await()
                        val user = auth.currentUser
                        user?.let {
                            val userData = hashMapOf(
                                "nombre" to "Nuevo usuario",
                                "email" to email,
                                "mascotas" to listOf<String>(),
                                "fechaRegistro" to FieldValue.serverTimestamp()
                            )

                            db.collection("usuarios").document(user.uid).set(userData).await()
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        } ?: run {
                            errorMessage = "Error al obtener usuario"
                        }
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        errorMessage = "La contraseña debe tener al menos 6 caracteres"
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        errorMessage = "Email inválido"
                    } catch (e: FirebaseAuthUserCollisionException) {
                        errorMessage = "El email ya está registrado"
                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.message}"
                    } finally {
                        isLoading = false
                        showRegisterDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun RegisterDialog(onDismiss: () -> Unit, onRegister: (String, String) -> Unit) {
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
                if (!acceptTerms) {
                    Toast.makeText(context, "Debe aceptar las condiciones de uso", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                onRegister(email, password)
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
