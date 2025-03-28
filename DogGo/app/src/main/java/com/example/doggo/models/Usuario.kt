package com.example.doggo.models

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val mascotas: List<String> = listOf() // Lista de IDs de mascotas
)