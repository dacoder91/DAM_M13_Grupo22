package com.example.doggo2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.viewinterop.AndroidView
import com.example.doggo2.controller.createMapView
import com.example.doggo2.controller.searchPlaces
import com.example.doggo2.controller.setupMap
import com.example.doggo2.R
import com.example.doggo2.ui.components.LogoutButton
import com.google.android.gms.location.LocationServices

@Composable
fun MapaScreen(
    navController: NavController,
    parentNavController: NavController
) {
    val auth = Firebase.auth
    var googleMap: GoogleMap? = null
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.imagenmapa),
            contentDescription = "Fondo difuminado",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        // Botón Salir
        LogoutButton(
            modifier = Modifier.align(Alignment.TopEnd),
            parentNavController = parentNavController
        )


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 70.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                item {
                    FilterButton("Pipicans") { searchPlaces(context, googleMap, "dogpark") }
                }
                item {
                    FilterButton("Veterinarios") { searchPlaces(context, googleMap, "veterinario") }
                }
                item {
                    FilterButton("Peluquerias") { searchPlaces(context, googleMap, "peluqueria canina") }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        createMapView(context) { map ->
                            googleMap = map
                            setupMap(context, map, fusedLocationClient)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { mapView -> mapView.onResume() },
                    onRelease = { mapView ->
                        mapView.onPause()
                        mapView.onDestroy()
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


//Funcion para crear el botón de filtro
@Composable
fun FilterButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(label)
    }
}

