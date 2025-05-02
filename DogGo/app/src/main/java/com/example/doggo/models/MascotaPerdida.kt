import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class MascotaPerdida(
    val id: String = "",
    val usuarioId: String = "",
    val nombreMascota: String = "",
    val fechaPerdida: Timestamp = Timestamp.now(),
    val ubicacion: GeoPoint = GeoPoint(0.0, 0.0),
    val fotoUrl: String = "",
    val encontrado: Boolean = false,
    val contacto : String = ""
)