// C:/Users/CND1508MJM/Documents/Universidad/Apps Moviles/Final/UniLocal/app/src/main/java/com/example/unilocal/ui/screens/HomeScreen.kt

package com.example.unilocal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.unilocal.data.model.Lugar
import com.example.unilocal.utils.HorarioUtils
import com.example.unilocal.viewmodel.AuthViewModel
import com.example.unilocal.viewmodel.LugarViewModel

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
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoria by remember { mutableStateOf<String?>(null) }

    val lugares by lugarViewModel.lugares.collectAsState()
    val loading by lugarViewModel.loading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val categorias = listOf(
        "Todos" to "🏠",
        "restaurante" to "🍽️",
        "cafeteria" to "☕",
        "museo" to "🏛️",
        "hotel" to "🏨",
        "comida_rapida" to "🍔"
    )

    LaunchedEffect(Unit) {
        lugarViewModel.cargarLugaresAprobados()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Hola, ${currentUser?.nombre?.split(" ")?.firstOrNull() ?: "Usuario"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Descubre lugares increíbles",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                // <-- INICIO DE LA MODIFICACIÓN -->
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onNavigateToLogin()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                // <-- FIN DE LA MODIFICACIÓN -->
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Inicio") },
                    label = { Text("Inicio", fontSize = 11.sp) },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Favorite, "Favoritos") },
                    label = { Text("Favoritos", fontSize = 11.sp) },
                    selected = false,
                    onClick = { onNavigateToFavoritos() }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            "Crear",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("Crear", fontSize = 11.sp) },
                    selected = false,
                    onClick = { onNavigateToCrear() },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Place, "Mis Lugares") },
                    label = { Text("Lugares", fontSize = 11.sp) },
                    selected = false,
                    onClick = { onNavigateToMisLugares() }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Perfil") },
                    label = { Text("Perfil", fontSize = 11.sp) },
                    selected = false,
                    onClick = { onNavigateToPerfil() }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Buscador
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (it.isNotEmpty()) {
                            lugarViewModel.buscarLugares(it, selectedCategoria)
                        } else {
                            lugarViewModel.cargarLugaresAprobados()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar lugares...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                lugarViewModel.cargarLugaresAprobados()
                            }) {
                                Icon(Icons.Default.Close, "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Chips de categorías
            LazyRow(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(categorias) { (categoria, emoji) ->
                    FilterChip(
                        selected = if (categoria == "Todos") selectedCategoria == null else selectedCategoria == categoria,
                        onClick = {
                            selectedCategoria = if (categoria == "Todos") null else categoria
                            lugarViewModel.buscarLugares(searchQuery, selectedCategoria)
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    categoria.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    fontSize = 13.sp
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (lugares.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No se encontraron lugares",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                if (lugar.imagenes.isNotEmpty()) {
                    AsyncImage(
                        model = lugar.imagenes.first(),
                        contentDescription = lugar.nombre,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Place,
                            null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = lugar.categoria.replace("_", " ").replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                val abierto = HorarioUtils.estaAbierto(lugar.horarioApertura, lugar.horarioCierre)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    color = if (abierto)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (abierto) "Abierto" else "Cerrado",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = lugar.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = lugar.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🕒", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
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
}