package com.example.unilocal.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilocal.data.model.Comentario
import com.example.unilocal.data.repository.FirebaseRepository
import com.example.unilocal.data.model.Lugar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LugarViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _lugares = MutableStateFlow<List<Lugar>>(emptyList())
    val lugares: StateFlow<List<Lugar>> = _lugares

    private val _lugarSeleccionado = MutableStateFlow<Lugar?>(null)
    val lugarSeleccionado: StateFlow<Lugar?> = _lugarSeleccionado

    private val _comentarios = MutableStateFlow<List<Comentario>>(emptyList())
    val comentarios: StateFlow<List<Comentario>> = _comentarios

    private val _misLugares = MutableStateFlow<List<Lugar>>(emptyList())
    val misLugares: StateFlow<List<Lugar>> = _misLugares

    private val _favoritos = MutableStateFlow<List<Lugar>>(emptyList())
    val favoritos: StateFlow<List<Lugar>> = _favoritos

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun cargarLugaresAprobados() {
        viewModelScope.launch {
            _loading.value = true
            _lugares.value = repository.getLugaresAprobados()
            _loading.value = false
        }
    }

    fun cargarMisLugares() {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId() ?: return@launch
            _misLugares.value = repository.getLugaresPorUsuario(userId)
        }
    }

    fun cargarFavoritos() {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId() ?: return@launch
            _favoritos.value = repository.getLugaresFavoritos(userId)
        }
    }

    fun seleccionarLugar(lugar: Lugar) {
        _lugarSeleccionado.value = lugar
        _comentarios.value = emptyList() // Limpiar comentarios anteriores
        cargarComentarios(lugar.id)
    }

    fun seleccionarLugarPorId(lugarId: String) {
        viewModelScope.launch {
            _comentarios.value = emptyList() // Limpiar comentarios
            val lugar = _lugares.value.find { it.id == lugarId }
            if (lugar != null) {
                seleccionarLugar(lugar)
            } else {
                // Si no está en la lista, recargar lugares
                cargarLugaresAprobados()
                kotlinx.coroutines.delay(500)
                val lugarActualizado = _lugares.value.find { it.id == lugarId }
                lugarActualizado?.let { seleccionarLugar(it) }
            }
        }
    }

    private fun cargarComentarios(lugarId: String) {
        viewModelScope.launch {
            _comentarios.value = repository.getComentariosPorLugar(lugarId)
        }
    }

    suspend fun crearLugar(lugar: Lugar, imagenesUri: List<Uri>): Boolean {
        _loading.value = true
        return try {
            val urls = mutableListOf<String>()
            imagenesUri.forEach { uri ->
                val result = repository.subirImagen(uri)
                if (result.isSuccess) {
                    urls.add(result.getOrNull()!!)
                }
            }

            val userId = repository.getCurrentUserId() ?: return false
            val nuevoLugar = lugar.copy(
                imagenes = urls,
                creadoPor = userId
            )

            val result = repository.crearLugar(nuevoLugar)
            _loading.value = false

            if (result.isSuccess) {
                _message.value = "Lugar creado, pendiente de aprobación"
                true
            } else {
                _message.value = result.exceptionOrNull()?.message
                false
            }
        } catch (e: Exception) {
            _loading.value = false
            _message.value = e.message
            false
        }
    }

    fun agregarComentario(texto: String, calificacion: Int) {
        viewModelScope.launch {
            val lugar = _lugarSeleccionado.value ?: return@launch
            val userId = repository.getCurrentUserId() ?: return@launch
            val user = repository.getCurrentUser() ?: return@launch

            val comentario = Comentario(
                lugarId = lugar.id,
                usuarioId = userId,
                usuarioNombre = user.nombre,
                texto = texto,
                calificacion = calificacion
            )

            val result = repository.agregarComentario(comentario)
            if (result.isSuccess) {
                // Recargar comentarios y lugares
                kotlinx.coroutines.delay(500) // Pequeña espera para que Firebase procese
                cargarComentarios(lugar.id)
                cargarLugaresAprobados()
            }
        }
    }

    fun responderComentario(comentarioId: String, respuesta: String) {
        viewModelScope.launch {
            repository.responderComentario(comentarioId, respuesta)
            _lugarSeleccionado.value?.let { cargarComentarios(it.id) }
        }
    }

    fun toggleFavorito(lugarId: String) {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId() ?: return@launch
            val esFavorito = _favoritos.value.any { it.id == lugarId }

            if (esFavorito) {
                repository.eliminarFavorito(userId, lugarId)
            } else {
                repository.agregarFavorito(userId, lugarId)
            }
            cargarFavoritos()
        }
    }

    fun eliminarLugar(lugarId: String) {
        viewModelScope.launch {
            repository.eliminarLugar(lugarId)
            cargarMisLugares()
        }
    }

    fun buscarLugares(query: String, categoria: String?) {
        viewModelScope.launch {
            _loading.value = true
            _lugares.value = repository.buscarLugares(query, categoria)
            _loading.value = false
        }
    }

    fun esFavorito(lugarId: String): Boolean {
        return _favoritos.value.any { it.id == lugarId }
    }

    fun clearMessage() {
        _message.value = null
    }
}