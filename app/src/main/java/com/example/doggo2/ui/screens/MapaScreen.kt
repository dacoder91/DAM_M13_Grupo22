package com.example.doggo2.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.google.android.gms.location.LocationServices
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
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.imagenmapa),
            contentDescription = "Fondo difuminado",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

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
private fun FilterButton(
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

// Funcion para crear el MapView
private fun createMapView(
    context: Context,
    onMapReady: (GoogleMap) -> Unit
): MapView {
    return MapView(context).apply {
        onCreate(null)
        getMapAsync { map -> onMapReady(map) }
    }
}

// Funcion para configurar el mapa
private fun setupMap(
    context: Context,
    map: GoogleMap,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient
) {
    try {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                    map.addMarker(
                        MarkerOptions()
                            .position(currentLocation)
                            .title("Ubicación Actual")
                    )
                } else {
                    android.util.Log.e("MapaScreen", "No se pudo obtener la ubicación actual.")
                }
            }.addOnFailureListener { exception ->
                android.util.Log.e("MapaScreen", "Error al obtener la ubicación: ${exception.message}")
            }
        } else {
            ActivityCompat.requestPermissions(
                (context as android.app.Activity),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    } catch (e: Exception) {
        android.util.Log.e("MapaScreen", "Error al configurar el mapa: ${e.message}")
    }
}

// Funcionn para validar la clave de la API
private fun validateApiKey(): Boolean {
    if (BuildConfig.MAPS_API_KEY.isNullOrEmpty()) {
        android.util.Log.e("MapaScreen", "La clave de la API no está configurada.")
        return false
    }
    return true
}

// Funcion para verificar la conectividad de red
private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

// Funciion para buscar lugares
private fun searchPlaces(
    context: Context,
    googleMap: GoogleMap?,
    keyword: String
) {
    if (googleMap == null) {
        android.util.Log.e("MapaScreen", "googleMap es nulo")
        return
    }

    if (!validateApiKey() || !isNetworkAvailable(context)) {
        android.util.Log.e("MapaScreen", "No se puede realizar la búsqueda: clave de API inválida o sin conexión.")
        return
    }

    try {
        val currentLocation = googleMap.cameraPosition.target
        val apiKey = BuildConfig.MAPS_API_KEY
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=${currentLocation.latitude},${currentLocation.longitude}" +
                "&radius=5000" +
                "&keyword=$keyword" +
                "&key=$apiKey"

        val requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context)
        val stringRequest = com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.GET, url,
            { response ->
                try {
                    val jsonObject = org.json.JSONObject(response)
                    val results = jsonObject.getJSONArray("results")
                    googleMap.clear()
                    for (i in 0 until results.length()) {
                        val place = results.getJSONObject(i)
                        val lat = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                        val lng = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                        val name = place.getString("name")
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat, lng))
                                .title(name)
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MapaScreen", "Error al procesar la respuesta: ${e.message}")
                }
            },
            { error ->
                android.util.Log.e("MapaScreen", "Error en la solicitud: ${error.message}")
            }
        )
        requestQueue.add(stringRequest)
    } catch (e: Exception) {
        android.util.Log.e("MapaScreen", "Error al buscar lugares: ${e.message}")
    }
}