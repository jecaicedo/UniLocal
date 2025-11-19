package com.example.unilocal.data.model

data class User(
    val id: String = "",
    val nombre: String = "",
    val username: String = "",
    val email: String = "",
    val ciudad: String = "",
    val rol: String = "usuario", // "usuario" o "moderador"
    val favoritos: List<String> = emptyList()
)