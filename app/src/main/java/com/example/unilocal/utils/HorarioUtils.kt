package com.example.unilocal.utils

import java.text.SimpleDateFormat
import java.util.*

object HorarioUtils {
    fun estaAbierto(horarioApertura: String, horarioCierre: String): Boolean {
        return try {
            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            val ahora = Calendar.getInstance()
            val horaActual = formato.format(ahora.time)

            val actual = formato.parse(horaActual)
            val apertura = formato.parse(horarioApertura)
            val cierre = formato.parse(horarioCierre)

            if (apertura != null && cierre != null && actual != null) {
                actual.after(apertura) && actual.before(cierre)
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}