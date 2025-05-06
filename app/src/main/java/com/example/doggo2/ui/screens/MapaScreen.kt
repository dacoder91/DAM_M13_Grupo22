package com.example.doggo2.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.doggo2.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.example.doggo2.BuildConfig
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.location.places.Place
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

@Composable
fun MapaScreen(
    navController: NavController,
    parentNavController: NavController
) {
    val auth = Firebase.auth
    var googleMap: GoogleMap? = null
    val context = LocalContext.current
    val placesClient = Places.createClient(context)

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo difuminado
        Image(
            painter = painterResource(id = R.drawable.imagenmapa),
            contentDescription = "Fondo difuminado",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        // Botón salir
        Button(
            onClick = {
                auth.signOut()
                parentNavController.navigate("login") {
                    popUpTo(0)
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(width = 110.dp, height = 35.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE91E63),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Salir")
        }

        Button(
            onClick = {
                googleMap?.let { map ->
                    val location = map.cameraPosition.target // Centro del mapa
                    val apiKey = BuildConfig.MAPS_API_KEY
                    val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                            "?location=${location.latitude},${location.longitude}" +
                            "&radius=5000" +
                            "&keyword=dogpark" +
                            "&key=$apiKey"

                    // Log para depurar el clic del botón
                    android.util.Log.d("MapaScreen", "Botón Pipicans presionado. URL: $url")

                    // Realizar la solicitud HTTP
                    val requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context)
                    val stringRequest = com.android.volley.toolbox.StringRequest(
                        com.android.volley.Request.Method.GET, url,
                        { response ->
                            // Log para depurar la respuesta
                            android.util.Log.d("MapaScreen", "Respuesta de la API: $response")

                            // Parsear la respuesta y agregar marcadores
                            val jsonObject = org.json.JSONObject(response)
                            val results = jsonObject.getJSONArray("results")
                            for (i in 0 until results.length()) {
                                val place = results.getJSONObject(i)
                                val lat = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                                val lng = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                                val name = place.getString("name")
                                map.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(lat, lng))
                                        .title(name)
                                )
                            }
                        },
                        { error ->
                            // Log para depurar errores
                            android.util.Log.e("MapaScreen", "Error en la solicitud: ${error.message}")
                        }
                    )
                    requestQueue.add(stringRequest)
                } ?: run {
                    android.util.Log.e("MapaScreen", "googleMap es nulo")
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Pipicans")
        }

        // Recuadro del mapa
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
                        MapView(context).apply {
                            onCreate(null)
                            getMapAsync { map ->
                                googleMap = map
                                // Configurar la ubicación inicial
                                val initialLocation = LatLng(41.3879, 2.16992) // Barcelona
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 12f))

                                // Habilitar controles de zoom y gestos
                                map.uiSettings.isZoomControlsEnabled = true
                                map.uiSettings.isMyLocationButtonEnabled = true
                                map.uiSettings.isCompassEnabled = true

                                // Agregar un marcador inicial
                                map.addMarker(MarkerOptions().position(initialLocation).title("Barcelona"))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}