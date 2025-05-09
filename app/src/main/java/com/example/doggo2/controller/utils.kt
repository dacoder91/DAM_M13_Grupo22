package com.example.doggo2.controller

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.doggo2.BuildConfig
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.util.Calendar
import java.util.Date

// Función para calcular la edad
fun calculateAge(birthDate: Date): Int {
    val today = Calendar.getInstance()
    val birthCalendar = Calendar.getInstance().apply { time = birthDate }

    var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

    // Si aún no ha pasado el cumpleaños este año, restar un año
    if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
        age--
    }

    return age
}

// Funcion para crear el MapView
fun createMapView(
    context: Context,
    onMapReady: (GoogleMap) -> Unit
): MapView {
    return MapView(context).apply {
        onCreate(null)
        getMapAsync { map -> onMapReady(map) }
    }
}

// Funcion para configurar el mapa
fun setupMap(
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
                (context as Activity),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    } catch (e: Exception) {
        android.util.Log.e("MapaScreen", "Error al configurar el mapa: ${e.message}")
    }
}

// Funcion para validar la clave de la API
fun validateApiKey(): Boolean {
    if (BuildConfig.MAPS_API_KEY.isNullOrEmpty()) {
        android.util.Log.e("MapaScreen", "La clave de la API no está configurada.")
        return false
    }
    return true
}

// Funcion para verificar la conectividad de red
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

// Funciin para buscar lugares
fun searchPlaces(
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

// Función para enviar correo usando EmailJS
fun enviarCorreo(context: Context, userName: String, userEmail: String) {
    val url = "https://api.emailjs.com/api/v1.0/email/send"
    val queue = Volley.newRequestQueue(context)

    val params = JSONObject().apply {
        put("service_id", "service_pcxvx5t")
        put("template_id", "template_537zd0a")
        put("user_id", "mMkoBEUAmfCweGdYC")
        put("template_params", JSONObject().apply {
            put("user_name", userName) // Este debe coincidir con {{user_name}} en la plantilla
            put("user_email", userEmail) // Este debe coincidir con {{user_email}} en la plantilla
        })
    }

    val request = object : StringRequest(
        Method.POST, url,
        { response ->
            Log.d("EmailJS", "Correo enviado: $response") // Aquí se registrará "OK"
        },
        { error ->
            Log.e("EmailJS", "Error al enviar el correo: ${error.message}")
            error.networkResponse?.let {
                Log.e("EmailJS", "Código de error: ${it.statusCode}")
                Log.e("EmailJS", "Respuesta: ${String(it.data)}")
            }
        }
    ) {
        override fun getHeaders(): MutableMap<String, String> {
            return mutableMapOf("Content-Type" to "application/json")
        }

        override fun getBody(): ByteArray {
            return params.toString().toByteArray(Charsets.UTF_8)
        }
    }

    queue.add(request)
}

@Composable
fun ActionButton(
    text: String,
    iconRes: Int,
    bgColor: Color,
    textStyle: TextStyle,
    modifier: Modifier,
    shape: RoundedCornerShape,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = shape
    ) {
        Text(text, style = textStyle)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White
        )
    }
}