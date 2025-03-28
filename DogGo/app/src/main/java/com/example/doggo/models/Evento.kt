import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Evento(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val ubicacion: GeoPoint = GeoPoint(0.0, 0.0),
    val creadorId: String = "",
    val maxParticipantes: Int = 0,
    val participantes: List<String> = listOf(),
    val tipo: String = "" // "Paseo" o "Pipican"
)