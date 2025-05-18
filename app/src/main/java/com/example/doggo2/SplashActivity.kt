package com.example.doggo2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doggo2.ui.theme.PetCommunityTheme
import com.example.doggo2.R
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PetCommunityTheme {
                SplashScreenContent()
            }
        }
    }

    @Composable
    fun SplashScreenContent() {
        val context = LocalContext.current

        // Simula una carga con retraso de 2 segundos y pasa a MainActivity
        LaunchedEffect(Unit) {
            delay(2000)
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as ComponentActivity).finish()
        }

        // UI del Splash
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DogGo",
                    fontFamily = FontFamily(Font(R.font.yellowpeach)),
                    fontSize = 65.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(50.dp))

                Image(
                    painter = painterResource(id = R.drawable.iconohome1),
                    contentDescription = "Icono",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "Cargando...",
                    fontFamily = FontFamily(Font(R.font.yellowpeach)),
                    fontSize = 42.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
