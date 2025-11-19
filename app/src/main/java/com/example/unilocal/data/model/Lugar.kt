package com.example.unilocal.data.model

import com.google.firebase.Timestamp

data class Lugar(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "", // restaurante, cafeteria, museo, hotel
    val imagenes: List<String> = emptyList(),
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val telefono: String = "",
    val horarioApertura: String = "09:00",
    val horarioCierre: String = "18:00",
    val creadoPor: String = "",
    val estado: String = "pendiente", // pendiente, aprobado, rechazado
    val moderadorId: String = "",
    val fechaCreacion: Timestamp? = null,
    val calificacionPromedio: Double = 0.0
)