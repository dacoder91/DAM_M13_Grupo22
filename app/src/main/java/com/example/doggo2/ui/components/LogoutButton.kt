package com.example.doggo2.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// LogoutButton.kt
@Composable
fun LogoutButton(
    modifier: Modifier = Modifier,
    parentNavController: NavController
) {
    val context = LocalContext.current

    Button(
        onClick = {
            try {
                Firebase.auth.signOut()
                parentNavController.navigate("login") {
                    popUpTo(0)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cerrar sesión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        },
        modifier = modifier // ← Usamos el modificador externo
            .padding(12.dp)
            .size(width = 65.dp, height = 35.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE91E63),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "")
        Spacer(modifier = Modifier.width(4.dp))
    }
}
