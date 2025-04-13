package com.example.doggo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doggo.ui.navigation.AppNavigation
import com.example.doggo.ui.screens.LoginScreen
import com.example.doggo.ui.theme.PetCommunityTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        setContent {
            PetCommunityTheme {
                val navController = rememberNavController()
                val startDestination = if (auth.currentUser != null) "main" else "login"
                AppNavigation(navController = navController, startDestination = startDestination)
            }
        }
    }
}
