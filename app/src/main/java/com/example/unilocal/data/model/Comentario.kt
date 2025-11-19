package com.example.unilocal.data.model

import com.google.firebase.Timestamp

data class Comentario(
    val id: String = "",
    val lugarId: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val texto: String = "",
    val calificacion: Int = 0,
    val fecha: Timestamp? = null,
    val respuesta: String = ""
)