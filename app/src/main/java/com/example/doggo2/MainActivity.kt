package com.example.doggo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.doggo2.ui.theme.PetCommunityTheme
import com.example.doggo2.ui.navigation.AppNavigation
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
                AppNavigation(navController = navController)
            }
        }
    }
}
