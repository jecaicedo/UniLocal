package com.example.unilocal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilocal.data.model.Lugar
import com.example.unilocal.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ModeradorViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _lugaresPendientes = MutableStateFlow<List<Lugar>>(emptyList())
    val lugaresPendientes: StateFlow<List<Lugar>> = _lugaresPendientes

    private val _lugaresAprobados = MutableStateFlow<List<Lugar>>(emptyList())
    val lugaresAprobados: StateFlow<List<Lugar>> = _lugaresAprobados

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun cargarLugaresPendientes() {
        viewModelScope.launch {
            _loading.value = true
            _lugaresPendientes.value = repository.getLugaresPendientes()
            _loading.value = false
        }
    }

    fun cargarLugaresAprobados() {
        viewModelScope.launch {
            val moderadorId = repository.getCurrentUserId() ?: return@launch
            _lugaresAprobados.value = repository.getLugaresAprobadosPorModerador(moderadorId)
        }
    }

    fun aprobarLugar(lugarId: String) {
        viewModelScope.launch {
            val moderadorId = repository.getCurrentUserId() ?: return@launch
            repository.aprobarLugar(lugarId, moderadorId)
            cargarLugaresPendientes()
            cargarLugaresAprobados()
        }
    }

    fun rechazarLugar(lugarId: String) {
        viewModelScope.launch {
            val moderadorId = repository.getCurrentUserId() ?: return@launch
            repository.rechazarLugar(lugarId, moderadorId)
            cargarLugaresPendientes()
        }
    }
}