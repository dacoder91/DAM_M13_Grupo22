package com.example.doggo.models

data class Mascota(
    val id: String = "",
    val nombre: String = "",
    val raza: String = "",
    val edad: Int = 0,
    val fotoUrl: String = "https://cdn-icons-png.flaticon.com/512/9769/9769450.png",
    val usuarioId: String = ""
)