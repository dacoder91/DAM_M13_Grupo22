package com.example.doggo2.ui.screens


import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.doggo2.R
import com.example.doggo2.controller.getCityFromGeoPoint
import com.example.doggo2.models.MascotaPerdida
import com.example.doggo2.ui.components.CustomButton
import com.example.doggo2.ui.components.LogoutButton
import com.example.doggo2.ui.screens.ui.theme.YellowPeach
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun MascotasPerdidasScreen(
    navController: NavController,
    parentNavController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var mascotasPerdidas by remember { mutableStateOf<List<MascotaPerdida>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMascota by remember { mutableStateOf<MascotaPerdida?>(null) }
    var mostrarEncontradas by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    val showHeader by remember { mutableStateOf(true) }

    // Escuchar cambios en la colección de mascotas perdidas
    LaunchedEffect(mostrarEncontradas) {
        val query = if (mostrarEncontradas) {
            db.collection("mascotasPerdidas")
                .orderBy("fechaPerdida", Query.Direction.DESCENDING)
        } else {
            db.collection("mascotasPerdidas")
                .whereEqualTo("encontrado", false)
                .orderBy("fechaPerdida", Query.Direction.DESCENDING)
        }

        query.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                mascotasPerdidas = snapshot.documents.mapNotNull { document ->
                    document.toObject(MascotaPerdida::class.java)?.copy(id = document.id)
                }
            }
        }
    }

    // A continuación se define el diseño de la pantalla
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.imagenperroperdido),
            contentDescription = "Fondo difuminado",
            contentScale = ContentScale.Crop,
            alpha = 0.5f,
            modifier = Modifier.fillMaxSize()
        )

        // Botón Salir
        LogoutButton(
            modifier = Modifier.align(Alignment.TopEnd),
            parentNavController = parentNavController
        )

        AnimatedVisibility(
            visible = showHeader,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_perdidos),
                    contentDescription = "Icono Perdidos",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Mascotas Perdidas",
                    style = TextStyle(
                        fontFamily = YellowPeach,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Checkbox para mostrar encontradas
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Checkbox(
                        checked = mostrarEncontradas,
                        onCheckedChange = { mostrarEncontradas = it }
                    )
                    Text(
                        text = "Mostrar encontradas",
                        style = TextStyle(
                            fontFamily = YellowPeach,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                //lazycolumn para mostrar las mascotas perdidas
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mascotasPerdidas) { mascota ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Indicador de mascota encontrada
                                if (mascota.encontrado) {
                                    Text(
                                        text = "ENCONTRADA",
                                        color = Color.Green,
                                        style = TextStyle(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (mascota.fotoUrl.isNotEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(mascota.fotoUrl),
                                            contentDescription = "Foto de la mascota",
                                            modifier = Modifier
                                                .size(192.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.iconoperro),
                                            contentDescription = "Sin imagen",
                                            modifier = Modifier
                                                .size(192.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row {
                                    Text("Nombre: ", fontWeight = FontWeight.Bold)
                                    Text(" ${mascota.nombreMascota}")
                                }

                                Row {
                                    Text("Fecha publicación: ", fontWeight = FontWeight.Bold)
                                    Text(
                                        "${
                                            SimpleDateFormat(
                                                "dd/MM/yyyy",
                                                Locale.getDefault()
                                            ).format(mascota.fechaPerdida.toDate())
                                        }"
                                    )
                                }

                                Row {
                                    Text("Ubicación: ", fontWeight = FontWeight.Bold)
                                    Text("${getCityFromGeoPoint(context, mascota.ubicacion)}")
                                }

                                Row {
                                Text("Contacto: ", fontWeight = FontWeight.Bold)
                                Text("${mascota.contacto}")
                                    }

                                Spacer(modifier = Modifier.height(8.dp))

                                //esta Row contiene los botones de editar y eliminar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (mascota.usuarioId == Firebase.auth.currentUser?.uid) {
                                        IconButton(onClick = {
                                            selectedMascota = mascota
                                            showEditDialog = true
                                        }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(onClick = {
                                            db.collection("mascotasPerdidas").document(mascota.id)
                                                .delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Mascota eliminada",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = mascota.encontrado,
                                                onCheckedChange = { isChecked ->
                                                    db.collection("mascotasPerdidas")
                                                        .document(mascota.id)
                                                        .update("encontrado", isChecked)
                                                        .addOnSuccessListener {
                                                            Toast.makeText(
                                                                context,
                                                                "Estado actualizado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                }
                                            )
                                            Text(
                                                text = "¿Encontrado?",
                                                style = TextStyle(
                                                    fontFamily = YellowPeach,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            )
                                        }
                                    }

                                    // Botón para ver ubicación en el mapa
                                    CustomButton(
                                        text = "Ver ubicación",
                                        onClick = {
                                            selectedMascota = mascota
                                            showMapDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                CustomButton(
                    text = "Añadir mascota perdida",
                    icon = painterResource(id = R.drawable.ic_mas),
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )
            }
        }
    }

    // Diálogo para añadir una nueva mascota perdida
    if (showAddDialog) {
        AddLostPetDialog(
            onDismiss = { showAddDialog = false },
            onSave = { nuevaMascota ->
                db.collection("mascotasPerdidas").add(nuevaMascota)
                    .addOnSuccessListener {
                        Toast.makeText(navController.context, "Mascota añadida", Toast.LENGTH_SHORT).show()
                        showAddDialog = false
                    }
            }
        )
    }

    // Diálogo para editar una mascota perdida
    if (showEditDialog && selectedMascota != null) {
        EditLostPetDialog(
            mascota = selectedMascota!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedMascota ->
                db.collection("mascotasPerdidas").document(updatedMascota.id).set(updatedMascota)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Anuncio actualizado", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    }
            }
        )
    }

    // Diálogo para mostrar la ubicación en el mapa
    if (showMapDialog && selectedMascota != null) {
        MapDialog2(
            initialLocation = selectedMascota!!.ubicacion,
            onDismiss = { showMapDialog = false },
            onLocationSelected = { /* No se necesita acción aquí */ }
        )
    }
}

