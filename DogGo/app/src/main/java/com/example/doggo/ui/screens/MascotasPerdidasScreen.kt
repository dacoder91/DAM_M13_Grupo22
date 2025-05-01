
package com.example.doggo.ui.screens

import MascotaPerdida
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.doggo.R
import com.example.doggo.ui.screens.ui.theme.YellowPeach
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

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

    LaunchedEffect(Unit) {
        db.collection("mascotasPerdidas").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                mascotasPerdidas = snapshot.documents.mapNotNull { document ->
                    document.toObject(MascotaPerdida::class.java)?.copy(id = document.id)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.imagenperroperdido),
            contentDescription = "Fondo difuminado",
            contentScale = ContentScale.Crop,
            alpha = 0.5f,
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = {
                Firebase.auth.signOut()
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
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Salir")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Mascotas Perdidas",
                style = TextStyle(fontFamily = YellowPeach, fontSize = 28.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
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
                                        painter = painterResource(id = R.drawable.imagenperroperdido),
                                        contentDescription = "Sin imagen",
                                        modifier = Modifier
                                            .size(192.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Nombre: ${mascota.nombreMascota}")
                            Text("Fecha publicación: ${mascota.fechaPerdida.toDate()}")
                            Text("Ubicación: ${mascota.ubicacion.latitude}, ${mascota.ubicacion.longitude}")

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IconButton(onClick = {
                                    selectedMascota = mascota
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(onClick = {
                                    db.collection("mascotasPerdidas").document(mascota.id).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                                        }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir anuncio de mascota perdida")
            }
        }
    }

    @Composable
    fun AddLostPetDialog(
        onDismiss: () -> Unit,
        onSave: (MascotaPerdida) -> Unit
    ) {
        if (showAddDialog) {
            AddLostPetDialog(
                onDismiss = { showAddDialog = false },
                onSave = { mascota ->
                    db.collection("mascotasPerdidas").add(mascota)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Anuncio agregado", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                        }
                }
            )
        }
    }

    @Composable
    fun EditLostPetDialog(
        mascota: MascotaPerdida,
        onDismiss: () -> Unit,
        onSave: (MascotaPerdida) -> Unit
    ) {
        if (showEditDialog && selectedMascota != null) {
            EditLostPetDialog(
                mascota = selectedMascota!!,
                onDismiss = { showEditDialog = false },
                onSave = { updated ->
                    db.collection("mascotasPerdidas").document(updated.id).set(updated)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Anuncio actualizado", Toast.LENGTH_SHORT)
                                .show()
                            showEditDialog = false
                        }
                }
            )
        }
    }
}
