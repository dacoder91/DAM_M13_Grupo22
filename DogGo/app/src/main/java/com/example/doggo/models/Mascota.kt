package com.example.doggo.models

data class Mascota(
    val id: String = "",
    val nombre: String = "",
    val raza: String = "",
    val edad: Int = 0,
    val fotoUrl: String = "",
    val usuarioId: String = ""
)