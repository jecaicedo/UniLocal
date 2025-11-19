package com.example.unilocal.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unilocal.data.model.Lugar
import com.example.unilocal.viewmodel.LugarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLugarScreen(
    lugarViewModel: LugarViewModel,
    onNavigateBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("restaurante") }
    var telefono by remember { mutableStateOf("") }
    var horarioApertura by remember { mutableStateOf("09:00") }
    var horarioCierre by remember { mutableStateOf("18:00") }
    var latitud by remember { mutableStateOf("7.1193") }
    var longitud by remember { mutableStateOf("-73.1227") }
    var imagenesUri by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showCategoriaMenu by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val loading by lugarViewModel.loading.collectAsState()
    val message by lugarViewModel.message.collectAsState()

    val categorias = listOf("restaurante", "cafeteria", "museo", "hotel", "comida_rapida")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            imagenesUri = uris.take(3) // Máximo 3 imágenes
            errorMessage = "Imágenes seleccionadas: ${uris.size}"
        }
    }

    LaunchedEffect(message) {
        message?.let {
            // Mostrar mensaje
            lugarViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Lugar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Lugar") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de Categoría
            ExposedDropdownMenuBox(
                expanded = showCategoriaMenu,
                onExpandedChange = { showCategoriaMenu = it }
            ) {
                OutlinedTextField(
                    value = categoria.capitalize(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoriaMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showCategoriaMenu,
                    onDismissRequest = { showCategoriaMenu = false }
                ) {
                    categorias.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.capitalize()) },
                            onClick = {
                                categoria = cat
                                showCategoriaMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = horarioApertura,
                    onValueChange = { horarioApertura = it },
                    label = { Text("Apertura (HH:mm)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = horarioCierre,
                    onValueChange = { horarioCierre = it },
                    label = { Text("Cierre (HH:mm)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ubicación (Bucaramanga por defecto)",
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = latitud,
                    onValueChange = { latitud = it },
                    label = { Text("Latitud") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = longitud,
                    onValueChange = { longitud = it },
                    label = { Text("Longitud") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("📷 Seleccionar Imágenes (${imagenesUri.size})")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && descripcion.isNotBlank() &&
                        telefono.isNotBlank() && imagenesUri.isNotEmpty()) {

                        val lugar = Lugar(
                            nombre = nombre,
                            descripcion = descripcion,
                            categoria = categoria,
                            telefono = telefono,
                            horarioApertura = horarioApertura,
                            horarioCierre = horarioCierre,
                            latitud = latitud.toDoubleOrNull() ?: 7.1193,
                            longitud = longitud.toDoubleOrNull() ?: -73.1227
                        )

                        scope.launch {
                            val success = lugarViewModel.crearLugar(lugar, imagenesUri)
                            if (success) {
                                onNavigateBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Lugar")
                }
            }

            message?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}