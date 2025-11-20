package com.example.unilocal.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val loading by lugarViewModel.loading.collectAsState()
    val message by lugarViewModel.message.collectAsState()

    val categorias = listOf(
        "restaurante" to "🍽️",
        "cafeteria" to "☕",
        "museo" to "🏛️",
        "hotel" to "🏨",
        "comida_rapida" to "🍔"
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            imagenesUri = uris.take(3)
            errorMessage = "✅ ${uris.size} imagen(es) seleccionada(s)"
        }
    }

    LaunchedEffect(message) {
        message?.let {
            errorMessage = it
            lugarViewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                "Volver",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nuevo Lugar",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Comparte tu lugar favorito con la comunidad",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del lugar") },
                        leadingIcon = { Icon(Icons.Default.Place, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        leadingIcon = { Icon(Icons.Default.Info, null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selector de Categoría con chips
                    Text(
                        "Categoría",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorias.take(3).forEach { (cat, emoji) ->
                            FilterChip(
                                selected = categoria == cat,
                                onClick = { categoria = cat },
                                label = { Text("$emoji ${cat.replace("_", " ").replaceFirstChar { it.uppercase() }}") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorias.drop(3).forEach { (cat, emoji) ->
                            FilterChip(
                                selected = categoria == cat,
                                onClick = { categoria = cat },
                                label = { Text("$emoji ${cat.replace("_", " ").replaceFirstChar { it.uppercase() }}") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Horario de atención",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = horarioApertura,
                            onValueChange = { horarioApertura = it },
                            label = { Text("Apertura") },
                            placeholder = { Text("09:00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = horarioCierre,
                            onValueChange = { horarioCierre = it },
                            label = { Text("Cierre") },
                            placeholder = { Text("18:00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Ubicación (Opcional)",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = latitud,
                            onValueChange = { latitud = it },
                            label = { Text("Latitud") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = longitud,
                            onValueChange = { longitud = it },
                            label = { Text("Longitud") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📷")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seleccionar Imágenes (${imagenesUri.size}/3)")
                    }

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = if (errorMessage.startsWith("✅"))
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            errorMessage = ""
                            when {
                                nombre.isBlank() -> errorMessage = "❌ El nombre es requerido"
                                descripcion.isBlank() -> errorMessage = "❌ La descripción es requerida"
                                telefono.isBlank() -> errorMessage = "❌ El teléfono es requerido"
                                imagenesUri.isEmpty() -> errorMessage = "❌ Debes seleccionar al menos una imagen"
                                else -> {
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
                                        errorMessage = "⏳ Subiendo imágenes..."
                                        val success = lugarViewModel.crearLugar(lugar, imagenesUri)
                                        if (success) {
                                            onNavigateBack()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !loading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Crear Lugar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}