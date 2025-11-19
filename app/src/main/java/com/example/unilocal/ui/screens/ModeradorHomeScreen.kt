package com.example.unilocal.ui.screens

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
import com.example.unilocal.viewmodel.AuthViewModel
import com.example.unilocal.viewmodel.ModeradorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeradorHomeScreen(
    moderadorViewModel: ModeradorViewModel,
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    val lugaresPendientes by moderadorViewModel.lugaresPendientes.collectAsState()
    val lugaresAprobados by moderadorViewModel.lugaresAprobados.collectAsState()
    val loading by moderadorViewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        moderadorViewModel.cargarLugaresPendientes()
        moderadorViewModel.cargarLugaresAprobados()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Moderador") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Menu, "Menú")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pendientes (${lugaresPendientes.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Aprobados (${lugaresAprobados.size})") }
                )
            }

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> LugaresPendientesList(
                        lugares = lugaresPendientes,
                        onAprobar = { moderadorViewModel.aprobarLugar(it) },
                        onRechazar = { moderadorViewModel.rechazarLugar(it) }
                    )
                    1 -> LugaresAprobadosList(lugares = lugaresAprobados)
                }
            }
        }
    }
}

@Composable
fun LugaresPendientesList(
    lugares: List<Lugar>,
    onAprobar: (String) -> Unit,
    onRechazar: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (lugares.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay lugares pendientes")
                }
            }
        }

        items(lugares) { lugar ->
            LugarModeradorCard(
                lugar = lugar,
                onAprobar = { onAprobar(lugar.id) },
                onRechazar = { onRechazar(lugar.id) }
            )
        }
    }
}

@Composable
fun LugaresAprobadosList(lugares: List<Lugar>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (lugares.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No has aprobado lugares aún")
                }
            }
        }

        items(lugares) { lugar ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    if (lugar.imagenes.isNotEmpty()) {
                        AsyncImage(
                            model = lugar.imagenes.first(),
                            contentDescription = lugar.nombre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = lugar.nombre,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = lugar.categoria.capitalize(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lugar.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LugarModeradorCard(
    lugar: Lugar,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (lugar.imagenes.isNotEmpty()) {
                AsyncImage(
                    model = lugar.imagenes.first(),
                    contentDescription = lugar.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = lugar.nombre,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lugar.categoria.capitalize(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = lugar.descripcion,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(lugar.telefono, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${lugar.horarioApertura} - ${lugar.horarioCierre}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAprobar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar")
                    }

                    OutlinedButton(
                        onClick = onRechazar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}