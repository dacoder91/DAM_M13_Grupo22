package com.example.doggo2.controller

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.json.JSONObject
import java.util.Calendar
import java.util.Date
import android.location.Geocoder
import java.util.Locale
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

/**
 * Calcula la edad en años a partir de una fecha de nacimiento dada.
 *
 * @param birthDate La [Date] de nacimiento.
 * @return La edad calculada como [Int].
 */
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

/**
 * Valida si una fecha (en milisegundos) es anterior al día actual.
 * Muestra un [Toast] si la fecha no es válida.
 *
 * @param context El [Context] de la aplicación para mostrar el Toast.
 * @param fecha La fecha a validar, representada como milisegundos desde la época. Puede ser nulo.
 * @return `true` si la fecha es válida (anterior a hoy y no nula), `false` en caso contrario.
 */
fun validarFechaInferiorAHoy(context: Context, fecha: Long?): Boolean {
    if (fecha == null || fecha > System.currentTimeMillis()) {
        Toast.makeText(context, "La fecha debe ser inferior a hoy", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

/**
 * Valida si una fecha de evento (en milisegundos) es igual o posterior al día actual.
 * Muestra un [Toast] si la fecha no es válida.
 *
 * @param context El [Context] de la aplicación para mostrar el Toast.
 * @param fecha La fecha a validar, representada como milisegundos desde la época. Puede ser nulo.
 * @return `true` si la fecha es válida (igual o superior a hoy y no nula), `false` en caso contrario.
 */
fun validarFechaSuperiorOIgualAHoy(context: Context, fecha: Long?): Boolean {
    if (fecha == null || fecha < System.currentTimeMillis()) {
        Toast.makeText(context, "La fecha debe ser igual o superior a hoy", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

/**
 * Crea y configura una instancia de [MapView].
 * Importante: El ciclo de vida de MapView (onCreate, onResume, onPause, etc.) debe ser manejado
 * por el componente que lo utiliza.
 *
 * @param context El [Context] de la aplicación.
 * @param onMapReady Callback que se invoca cuando el [GoogleMap] está listo para ser usado.
 * @return Una instancia de [MapView].
 */
fun createMapView(
    context: Context,
    onMapReady: (GoogleMap) -> Unit
): MapView {
    return MapView(context).apply {
        onCreate(null)
        getMapAsync { map -> onMapReady(map) }
    }
}

/**
 * Configura un [GoogleMap] para mostrar la ubicación actual del usuario y un marcador.
 * Solicita permisos de ubicación si no están concedidos.
 *
 * @param context El [Context] de la aplicación. Debe ser una [Activity] para solicitar permisos.
 * @param map La instancia de [GoogleMap] a configurar.
 * @param fusedLocationClient El cliente [com.google.android.gms.location.FusedLocationProviderClient] para obtener la última ubicación conocida.
 */
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

/**
 * Valida si la clave de la API de Google Maps está configurada en `BuildConfig`.
 *
 * @return `true` si la clave de la API es válida y no está vacía, `false` en caso contrario.
 */
fun validateApiKey(): Boolean {
    if (BuildConfig.MAPS_API_KEY.isNullOrEmpty()) {
        android.util.Log.e("MapaScreen", "La clave de la API no está configurada.")
        return false
    }
    return true
}

/**
 * Verifica si hay una conexión de red activa disponible en el dispositivo.
 *
 * @param context El [Context] de la aplicación.
 * @return `true` si hay una conexión de red disponible, `false` en caso contrario.
 */
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}


/**
 * Busca lugares cercanos en el mapa usando la API de Google Places.
 *
 * @param context Contexto de la aplicación.
 * @param googleMap Instancia del mapa donde se mostrarán los resultados.
 * @param keyword Palabra clave para buscar (ej. "pipican", "veterinario").
 *
 * Valida conexión y clave de API antes de realizar la búsqueda.
 * Añade marcadores en el mapa con los resultados obtenidos.
 */

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


/**
 * Envía un correo de bienvenida usando el servicio EmailJS.
 *
 * @param context Contexto de la aplicación.
 * @param userName Nombre del usuario a incluir en la plantilla.
 * @param userEmail Correo del usuario destinatario.
 *
 * Utiliza Volley para enviar una solicitud POST a la API de EmailJS con los parámetros definidos.
 * Registra en Logcat el resultado del envío o cualquier error.
 */

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


/**
 * Crea un botón de acción personalizado con texto e ícono.
 *
 * @param text Texto del botón.
 * @param iconRes Recurso del ícono.
 * @param bgColor Color de fondo.
 * @param textStyle Estilo del texto.
 * @param modifier Modificador de estilo y tamaño.
 * @param shape Forma del botón.
 * @param onClick Acción al pulsar el botón.
 */
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


/**
 * Valida los campos obligatorios de un evento.
 *
 * @return true si todos los campos son válidos, false si falta alguno.
 */

fun validarCamposEvento(
    titulo: String,
    descripcion: String,
    ubicacion: GeoPoint,
    fecha: Long?,
    tipo: String,
    context: Context
): Boolean {
    if (titulo.isBlank() || descripcion.isBlank() || tipo.isBlank() || fecha == null ||
        (ubicacion.latitude == 0.0 && ubicacion.longitude == 0.0)) {
        Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}


/**
 * Envía un mensaje al chat de un evento en Firestore.
 *
 * @param eventoId ID del evento.
 * @param senderId ID del remitente.
 * @param texto Contenido del mensaje.
 */

fun enviarMensaje(eventoId: String, senderId: String, texto: String) {
    val db = FirebaseFirestore.getInstance()
    val mensaje = hashMapOf(
        "senderId" to senderId,
        "text" to texto,
        "timestamp" to FieldValue.serverTimestamp()
    )

    db.collection("Eventos")
        .document(eventoId)
        .collection("Chats")
        .add(mensaje)
        .addOnSuccessListener {
            Log.d("Mensaje", "Mensaje enviado correctamente")
        }
        .addOnFailureListener { e ->
            Log.e("Mensaje", "Error al enviar mensaje", e)
        }
}



/**
 * Obtiene el nombre de la ciudad a partir de un GeoPoint.
 *
 * @param geoPoint Coordenadas geográficas.
 * @return Nombre de la ciudad o mensaje de error.
 */

fun getCityFromGeoPoint(context: Context, geoPoint: GeoPoint): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            addresses[0].locality ?: "Ciudad desconocida"
        } else {
            "Ciudad desconocida"
        }
    } catch (e: Exception) {
        "Error al obtener ciudad"
    }
}


/**
 * Composable para seleccionar y subir una imagen a Firebase Storage.
 *
 * @param onUploadComplete Callback con la URL de la imagen subida.
 */
@Composable
fun SelectAndUploadPhoto(
    onUploadComplete: (String) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploadPhotoToFirebase(uri, onUploadComplete)
        } else {
            Log.e("UploadPhoto", "No se seleccionó ninguna imagen")
        }
    }

    Button(onClick = { launcher.launch("image/*") }) {
        Text("Subir foto")
    }
}


/**
 * Sube una imagen a Firebase Storage y devuelve su URL.
 *
 * @param imageUri URI de la imagen seleccionada.
 * @param onUploadComplete Callback con la URL de descarga.
 */
fun uploadPhotoToFirebase(imageUri: Uri, onUploadComplete: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val fileName = "images/${UUID.randomUUID()}.jpg"
    val fileRef = storageRef.child(fileName)

    fileRef.putFile(imageUri)
        .addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                onUploadComplete(uri.toString())
            }
        }
        .addOnFailureListener { e ->
            Log.e("UploadPhoto", "Error al subir la foto: ${e.message}", e)
        }
}