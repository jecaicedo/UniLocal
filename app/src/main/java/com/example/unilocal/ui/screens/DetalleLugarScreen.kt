package com.example.unilocal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.unilocal.utils.HorarioUtils
import com.example.unilocal.viewmodel.LugarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleLugarScreen(
    lugarId: String,
    lugarViewModel: LugarViewModel,
    onNavigateBack: () -> Unit
) {
    var showComentarioDialog by remember { mutableStateOf(false) }
    var comentarioTexto by remember { mutableStateOf("") }
    var calificacion by remember { mutableStateOf(5) }

    val lugar by lugarViewModel.lugarSeleccionado.collectAsState()
    val comentarios by lugarViewModel.comentarios.collectAsState()

    LaunchedEffect(lugarId) {
        lugarViewModel.cargarLugaresAprobados()
        lugarViewModel.cargarFavoritos()
        lugarViewModel.seleccionarLugarPorId(lugarId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lugar?.nombre ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        lugar?.let { lugarViewModel.toggleFavorito(it.id) }
                    }) {
                        Icon(
                            if (lugar?.let { lugarViewModel.esFavorito(it.id) } == true)
                                Icons.Filled.Favorite
                            else
                                Icons.Filled.FavoriteBorder,
                            "Favorito"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showComentarioDialog = true }) {
                Icon(Icons.Default.Add, "Comentar")
            }
        }
    ) { padding ->
        lugar?.let { lugarActual ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Imágenes
                if (lugarActual.imagenes.isNotEmpty()) {
                    item {
                        AsyncImage(
                            model = lugarActual.imagenes.first(),
                            contentDescription = lugarActual.nombre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = lugarActual.nombre,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lugarActual.categoria.capitalize(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            val abierto = HorarioUtils.estaAbierto(
                                lugarActual.horarioApertura,
                                lugarActual.horarioCierre
                            )
                            Text(
                                text = if (abierto) "Abierto" else "Cerrado",
                                color = if (abierto)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", lugarActual.calificacionPromedio),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = lugarActual.descripcion,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(lugarActual.telefono)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${lugarActual.horarioApertura} - ${lugarActual.horarioCierre}")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Comentarios",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                items(comentarios) { comentario ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = comentario.usuarioNombre,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    comentario.fecha?.let {
                                        Text(
                                            text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                                .format(it.toDate()),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row {
                                    repeat(comentario.calificacion) {
                                        Icon(
                                            Icons.Default.Star,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = comentario.texto,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (comentario.respuesta.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Respuesta del propietario:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = comentario.respuesta,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (comentarios.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay comentarios aún. ¡Sé el primero en comentar!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showComentarioDialog) {
        AlertDialog(
            onDismissRequest = { showComentarioDialog = false },
            title = { Text("Agregar Comentario") },
            text = {
                Column {
                    Text("Calificación:", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(5) { index ->
                            IconButton(onClick = { calificacion = index + 1 }) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = if (index < calificacion)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = comentarioTexto,
                        onValueChange = { comentarioTexto = it },
                        label = { Text("Comentario") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (comentarioTexto.isNotBlank()) {
                            lugarViewModel.agregarComentario(comentarioTexto, calificacion)
                            comentarioTexto = ""
                            calificacion = 5
                            showComentarioDialog = false
                        }
                    },
                    enabled = comentarioTexto.isNotBlank()
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showComentarioDialog = false
                    comentarioTexto = ""
                    calificacion = 5
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}