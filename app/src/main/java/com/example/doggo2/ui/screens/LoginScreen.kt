package com.example.doggo2.ui.screens

import com.example.doggo2.R
import com.example.doggo2.ui.components.CustomButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.doggo2.controller.enviarCorreo
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


// Esta función representa la pantalla de inicio de sesión de la aplicación.
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

    // Variables de estado para manejar el inicio de sesión
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_huellitas),
                    contentDescription = "Icono de huellas",
                    modifier = Modifier.size(55.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "DogGo",
                    fontSize = 55.sp,
                    fontFamily = FontFamily(Font(R.font.yellowpeach)),
                    color = Color.Black
                )
            }


            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = errorMessage != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.doggo_pink),
                    cursorColor = colorResource(id = R.color.doggo_pink),
                    focusedLabelColor = colorResource(id = R.color.doggo_pink)
                )
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
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.doggo_pink),
                    cursorColor = colorResource(id = R.color.doggo_pink),
                    focusedLabelColor = colorResource(id = R.color.doggo_pink)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Iniciar Sesión
            CustomButton(
                text = if (isLoading) "" else "Iniciar sesión", // ocultamos texto si está cargando
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Por favor completa todos los campos"
                        return@CustomButton
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
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto para abrir el diálogo de registro
            TextButton(
                onClick = { showRegisterDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "¿No tienes cuenta? ¡Regístrate!",
                    color = colorResource(id = R.color.doggo_pink),
                    style = MaterialTheme.typography.bodyMedium
                )
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


    // Diálogo de registro
    if (showRegisterDialog) {
        val context = LocalContext.current // Obtén el contexto aquí
        RegisterDialog(
            onDismiss = { showRegisterDialog = false }, // Cierra el diálogo
            onRegister = { username, email, password -> // Llama a la función de registro
                scope.launch {
                    isLoading = true
                    try {
                        auth.createUserWithEmailAndPassword(email, password).await()
                        val user = auth.currentUser
                        user?.let {
                            val userData = hashMapOf(
                                "nombre" to username,
                                "email" to email,
                                "mascotas" to listOf<String>(),
                                "fechaRegistro" to FieldValue.serverTimestamp()
                            )

                            db.collection("usuarios").document(user.uid).set(userData).await()

                            // Enviar correo al usuario
                            enviarCorreo(context, username, email)

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

