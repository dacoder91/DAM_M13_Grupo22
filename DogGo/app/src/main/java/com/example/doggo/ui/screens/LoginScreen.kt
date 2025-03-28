package com.example.doggo.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DogGo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email") },
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
            visualTransformation = PasswordVisualTransformation(),
            isError = errorMessage != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Iniciar Sesión
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor completa todos los campos"
                    return@Button
                }

                isLoading = true
                scope.launch {
                    try {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = when (task.exception) {
                                        is FirebaseAuthInvalidUserException ->
                                            "Usuario no registrado"
                                        is FirebaseAuthInvalidCredentialsException ->
                                            "Credenciales incorrectas"
                                        else -> "Error: ${task.exception?.message}"
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Error al iniciar sesión"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Crear Cuenta
        TextButton(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor completa todos los campos"
                    return@TextButton
                }

                isLoading = true
                scope.launch {
                    try {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    user?.let {
                                        val userData = hashMapOf(
                                            "nombre" to "Nuevo usuario",
                                            "email" to email,
                                            "mascotas" to listOf<String>(),
                                            "fechaRegistro" to FieldValue.serverTimestamp()
                                        )

                                        db.collection("usuarios").document(user.uid)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                navController.navigate("main") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = "Error al guardar datos: ${e.message}"
                                            }
                                    } ?: run {
                                        errorMessage = "Error al obtener usuario"
                                    }
                                } else {
                                    errorMessage = when (task.exception) {
                                        is FirebaseAuthWeakPasswordException ->
                                            "La contraseña debe tener al menos 6 caracteres"
                                        is FirebaseAuthInvalidCredentialsException ->
                                            "Email inválido"
                                        is FirebaseAuthUserCollisionException ->
                                            "El email ya está registrado"
                                        else -> "Error: ${task.exception?.message}"
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Error al crear cuenta"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            Text("Crear nueva cuenta")
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