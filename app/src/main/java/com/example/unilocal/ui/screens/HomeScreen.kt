package com.example.unilocal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.unilocal.data.model.Lugar
import com.example.unilocal.utils.HorarioUtils
import com.example.unilocal.viewmodel.AuthViewModel
import com.example.unilocal.viewmodel.LugarViewModel
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    lugarViewModel: LugarViewModel,
    authViewModel: AuthViewModel,
    onNavigateToDetalle: (String) -> Unit,
    onNavigateToCrear: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToMisLugares: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoria by remember { mutableStateOf<String?>(null) }
    var showCategoriaMenu by remember { mutableStateOf(false) }

    val lugares by lugarViewModel.lugares.collectAsState()
    val loading by lugarViewModel.loading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val categorias = listOf("Todas", "restaurante", "cafeteria", "museo", "hotel", "comida_rapida")

    LaunchedEffect(Unit) {
        lugarViewModel.cargarLugaresAprobados()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UniLocal") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Menu, "Menú")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Perfil") },
                            onClick = {
                                showMenu = false
                                onNavigateToPerfil()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mis Lugares") },
                            onClick = {
                                showMenu = false
                                onNavigateToMisLugares()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Favoritos") },
                            onClick = {
                                showMenu = false
                                onNavigateToFavoritos()
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión") },
                            onClick = {
                                showMenu = false
                                authViewModel.logout()
                                onNavigateToLogin()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCrear) {
                Icon(Icons.Default.Add, "Crear Lugar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Buscador mejorado
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Buscar lugares...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalButton(onClick = { showCategoriaMenu = true }) {
                            Icon(Icons.Default.List, "Filtrar", modifier = Modifier.size(20.dp))
                        }

                        DropdownMenu(
                            expanded = showCategoriaMenu,
                            onDismissRequest = { showCategoriaMenu = false }
                        ) {
                            categorias.forEach { categoria ->
                                DropdownMenuItem(
                                    text = { Text(categoria.replace("_", " ").capitalize()) },
                                    onClick = {
                                        selectedCategoria = if (categoria == "Todas") null else categoria
                                        showCategoriaMenu = false
                                        lugarViewModel.buscarLugares(searchQuery, selectedCategoria)
                                    }
                                )
                            }
                        }
                    }

                    if (selectedCategoria != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = {
                                selectedCategoria = null
                                lugarViewModel.cargarLugaresAprobados()
                            },
                            label = { Text(selectedCategoria!!.replace("_", " ").capitalize()) },
                            leadingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lugares) { lugar ->
                        LugarCard(
                            lugar = lugar,
                            onClick = { onNavigateToDetalle(lugar.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LugarCard(
    lugar: Lugar,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            if (lugar.imagenes.isNotEmpty()) {
                Box {
                    AsyncImage(
                        model = lugar.imagenes.first(),
                        contentDescription = lugar.nombre,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Badge de estado
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = if (HorarioUtils.estaAbierto(lugar.horarioApertura, lugar.horarioCierre))
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (HorarioUtils.estaAbierto(lugar.horarioApertura, lugar.horarioCierre)) "Abierto" else "Cerrado",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = lugar.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = lugar.categoria.replace("_", " ").capitalize(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = lugar.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lugar.calificacionPromedio > 0)
                                String.format("%.1f", lugar.calificacionPromedio)
                            else "Sin calificar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${lugar.horarioApertura} - ${lugar.horarioCierre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}