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

    //Función principal de la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeServices()

        setContent {
            PetCommunityTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }

    // Inicializa Firebase Auth y Google Places API
    private fun initializeServices() {
        // Inicializa Firebase Auth
        auth = Firebase.auth

        // Inicializa Google Places API
        try {
            if (!Places.isInitialized()) {
                val apiKey = getString(R.string.maps_api_key)
                Places.initialize(applicationContext, apiKey)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al inicializar Google Places API: ${e.message}", e)
        }
    }
}