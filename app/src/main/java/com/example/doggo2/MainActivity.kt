package com.example.doggo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.doggo2.ui.theme.PetCommunityTheme
import com.example.doggo2.ui.navigation.AppNavigation
import com.google.android.libraries.places.api.Places
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import android.util.Log

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        // Inicializar la API de Google Places
        try {
            if (!Places.isInitialized()) {
                // Inicializa la API de Google Places con tu clave de API
                // Hay que asegurarse de que la clave de API esté configurada en el archivo strings.xml
                val apiKey = getString(R.string.maps_api_key)
                Places.initialize(applicationContext, apiKey)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al inicializar Google Places API: ${e.message}", e)
        }

        setContent {
            PetCommunityTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}