package com.example.tekkentournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Imports de tus paquetes
import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.repositories.TournamentRepository
import com.example.tekkentournaments.utils.TekkenData // NECESARIO PARA LA LISTA DE JUEGOS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentsListScreen(
    onBack: () -> Unit,
    onTournamentClick: (String) -> Unit
) {
    var tournaments by remember { mutableStateOf<List<Tournament>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Función para recargar la lista
    fun cargarTorneos() {
        scope.launch {
            isLoading = true
            tournaments = TournamentRepository.obtenerTorneos()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        cargarTorneos()
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TORNEOS", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFFD32F2F), // Rojo Tekken
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Crear Torneo")
            }
        }
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD32F2F))
                }
            } else if (tournaments.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay torneos disponibles.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tournaments) { torneo ->
                        TournamentItemCard(torneo, onClick = { onTournamentClick(torneo.id) })
                    }
                }
            }
        }
    }

    // --- DIÁLOGO PARA CREAR TORNEO ACTUALIZADO ---
    if (showCreateDialog) {
        CreateTournamentDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc, date, players, type, game -> // Añadido parámetro game
                scope.launch {
                    val success = TournamentRepository.crearTorneo(
                        nombre = name,
                        descripcion = desc,
                        fecha = date,
                        maxJugadores = players,
                        tipo = type,
                        juego = game // Pasamos el juego al repo
                    )
                    if (success) {
                        cargarTorneos() // Recargar lista
                        showCreateDialog = false
                    }
                }
            }
        )
    }
}

// --- TARJETA DE LISTA ---
@Composable
fun TournamentItemCard(tournament: Tournament, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = tournament.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    // Mostramos el juego en pequeño debajo del nombre
                    Text(
                        // Si gameVersion es null, mostramos "Tekken 8" por defecto
                        text = tournament.gameVersion ?: "Tekken 8",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Etiqueta de estado
                Surface(color = Color(0xFFD32F2F).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = if (tournament.status == "en_curso") "EN CURSO" else "ABIERTO",
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            if (!tournament.description.isNullOrBlank()) {
                Text(text = tournament.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFF333333))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = tournament.creatorName ?: "Anon", color = Color.Gray, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${tournament.maxPlayers} Max", color = Color.Gray, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = tournament.date ?: "TBD", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

// --- FORMULARIO DE CREACIÓN CON SELECTOR DE JUEGO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, String, String) -> Unit // Añadido String final para el juego
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2025-12-01") }
    var maxPlayers by remember { mutableStateOf("16") }
    var type by remember { mutableStateOf("Eliminación Simple") }

    // NUEVO: Estado para el juego
    var selectedGame by remember { mutableStateOf("Tekken 8") }
    var expandedGame by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Nuevo Torneo", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // 1. SELECTOR DE JUEGO (DROPDOWN)
                ExposedDropdownMenuBox(
                    expanded = expandedGame,
                    onExpandedChange = { expandedGame = !expandedGame }
                ) {
                    OutlinedTextField(
                        value = selectedGame,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Versión del Juego") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGame) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFFD32F2F)
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGame,
                        onDismissRequest = { expandedGame = false },
                        modifier = Modifier.background(Color(0xFF2C2C2C))
                    ) {
                        TekkenData.gameVersions.forEach { game ->
                            DropdownMenuItem(
                                text = { Text(game, color = Color.White) },
                                onClick = {
                                    selectedGame = game
                                    expandedGame = false
                                }
                            )
                        }
                    }
                }

                // 2. RESTO DE CAMPOS
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nombre del Torneo") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color(0xFFD32F2F)
                    )
                )

                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    label = { Text("Descripción") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color(0xFFD32F2F)
                    )
                )

                OutlinedTextField(
                    value = date, onValueChange = { date = it },
                    label = { Text("Fecha (YYYY-MM-DD)") },
                    singleLine = true,
                    trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color(0xFFD32F2F)
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Jugadores:", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf("8", "16", "32").forEach { num ->
                        FilterChip(
                            selected = maxPlayers == num,
                            onClick = { maxPlayers = num },
                            label = { Text(num) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD32F2F),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF333333),
                                labelColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        onConfirm(name, desc, date, maxPlayers.toIntOrNull() ?: 16, type, selectedGame)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("CREAR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        }
    )
}