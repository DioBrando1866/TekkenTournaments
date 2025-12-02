package com.example.tekkentournaments

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// IMPORTS DE TUS CLASES Y REPOSITORIOS
import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.clases.Player
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.repositories.TournamentRepository
import com.example.tekkentournaments.ui.components.BracketMatchCard // Asegúrate de haber actualizado este componente con onP1Click/onP2Click
import io.github.jan.supabase.auth.auth

// IMPORT GLOBAL DE SUPABASE (Si no lo tienes global, ajusta esto)
import supabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournamentId: String,
    onBack: () -> Unit,
    onTournamentDeleted: () -> Unit
) {
    // --- ESTADOS DE DATOS ---
    var tournament by remember { mutableStateOf<Tournament?>(null) }
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }

    // --- ESTADOS DE CONTROL ---
    var isCreator by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Info, 1: Players, 2: Bracket

    // --- ESTADOS DE DIÁLOGOS ---
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showStartDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current // Para mostrar Toasts

    // FUNCIÓN DE CARGA DE DATOS
    fun cargarDatos() {
        scope.launch {
            isLoading = true
            // 1. Obtener Torneo
            val t = TournamentRepository.obtenerTorneoPorId(tournamentId)
            tournament = t

            if (t != null) {
                // 2. Comprobar si soy el creador
                val currentUser = supabase.auth.currentUserOrNull()
                isCreator = (currentUser != null && t.creatorId == currentUser.id)

                // 3. Obtener Jugadores
                players = TournamentRepository.obtenerJugadores(tournamentId)

                // 4. Obtener Matches (Bracket)
                matches = TournamentRepository.obtenerMatches(tournamentId)
            }
            isLoading = false
        }
    }

    // Cargar al iniciar
    LaunchedEffect(Unit) { cargarDatos() }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            tournament?.name?.uppercase() ?: "CARGANDO...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF1E1E1E),
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    actions = {
                        // BOTONES DE ADMIN (Solo creador)
                        if (isCreator && !isLoading) {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Default.Edit, "Editar", tint = Color.White)
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, "Borrar", tint = Color(0xFFD32F2F))
                            }
                        }
                    }
                )

                // --- BARRA DE PESTAÑAS ---
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color(0xFFD32F2F),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFFD32F2F)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("INFO") },
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("JUGADORES (${players.size})") },
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("BRACKET") },
                        unselectedContentColor = Color.Gray
                    )
                }
            }
        },
        floatingActionButton = {
            // --- BOTONES FLOTANTES DE ACCIÓN ---
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. BOTÓN VERDE: EMPEZAR TORNEO
                if (isCreator && selectedTab == 1 && matches.isEmpty() && players.size >= 2) {
                    ExtendedFloatingActionButton(
                        onClick = { showStartDialog = true },
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("EMPEZAR")
                    }
                }

                // 2. BOTÓN ROJO: AÑADIR JUGADOR
                if (isCreator && selectedTab == 1 && matches.isEmpty()) {
                    FloatingActionButton(
                        onClick = { showAddPlayerDialog = true },
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, "Añadir")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFD32F2F)
                )
            } else if (tournament != null) {
                // CONTENIDO DE LAS PESTAÑAS
                when (selectedTab) {
                    0 -> InfoTab(tournament!!)
                    1 -> PlayersTab(players)
                    // PESTAÑA DEL BRACKET CON LÓGICA DE JUEGO
                    2 -> BracketTab(
                        matches = matches,
                        players = players,
                        isCreator = isCreator, // Pasamos permiso de creador
                        onMatchUpdate = { matchActualizado ->
                            // Callback para guardar en BD
                            scope.launch {
                                TournamentRepository.actualizarPartida(matchActualizado)
                                cargarDatos() // Refrescar UI
                            }
                        }
                    )
                }
            }
        }
    }

    // --- DIÁLOGOS ---

    // 1. AÑADIR JUGADOR
    if (showAddPlayerDialog) {
        AddPlayerDialog(
            onDismiss = { showAddPlayerDialog = false },
            onConfirm = { name ->
                scope.launch {
                    TournamentRepository.agregarJugador(tournamentId, name)
                    cargarDatos()
                    showAddPlayerDialog = false
                }
            }
        )
    }

    // 2. EMPEZAR TORNEO (Con selección de formato)
    if (showStartDialog) {
        StartTournamentDialog(
            onDismiss = { showStartDialog = false },
            onConfirm = { formato ->
                scope.launch {
                    val exito = TournamentRepository.generarBracketInicial(tournamentId, formato)
                    if (exito) {
                        android.widget.Toast.makeText(context, "¡Bracket Generado!", android.widget.Toast.LENGTH_SHORT).show()
                        cargarDatos()
                        selectedTab = 2
                    } else {
                        android.widget.Toast.makeText(context, "Error al generar. Revisa el Logcat.", android.widget.Toast.LENGTH_LONG).show()
                    }
                    showStartDialog = false
                }
            }
        )
    }

    // 3. BORRAR TORNEO
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("¿Eliminar Torneo?", color = Color.White) },
            text = { Text("Se borrarán todos los datos y partidas.", color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val exito = TournamentRepository.borrarTorneo(tournamentId)
                            if (exito) onTournamentDeleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("ELIMINAR") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("CANCELAR", color = Color.Gray) }
            }
        )
    }

    // 4. EDITAR TORNEO
    if (showEditDialog && tournament != null) {
        EditTournamentDialog(
            currentName = tournament!!.name,
            currentDesc = tournament!!.description ?: "",
            currentDate = tournament!!.date ?: "",
            currentMax = tournament!!.maxPlayers,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, desc, date, max ->
                scope.launch {
                    TournamentRepository.editarTorneo(tournamentId, name, desc, date, max)
                    cargarDatos()
                    showEditDialog = false
                }
            }
        )
    }
}

// ==========================================
// COMPONENTES DE LAS PESTAÑAS
// ==========================================

@Composable
fun InfoTab(tournament: Tournament) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("DESCRIPCIÓN", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            tournament.description ?: "Sin descripción.",
            color = Color.White,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                InfoRow(Icons.Default.VideogameAsset, "Juego", "Tekken 8")
                Divider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(Icons.Default.EmojiEvents, "Formato", tournament.tournamentType)
                Divider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(Icons.Default.CalendarToday, "Fecha", tournament.date ?: "TBD")
                Divider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(Icons.Default.Group, "Jugadores", "${tournament.maxPlayers} Max")
            }
        }
    }
}

@Composable
fun PlayersTab(players: List<Player>) {
    if (players.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay jugadores inscritos.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(players) { player ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.Gray)
                        Spacer(Modifier.width(16.dp))
                        Text(player.name, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BracketTab(
    matches: List<Match>,
    players: List<Player>,
    isCreator: Boolean,
    onMatchUpdate: (Match) -> Unit // Callback para guardar cambios
) {
    if (matches.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("El torneo no ha comenzado.", color = Color.Gray)
        }
        return
    }

    val context = LocalContext.current
    val rounds = matches.groupBy { it.round }.toSortedMap()
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .horizontalScroll(horizontalScroll)
                .verticalScroll(verticalScroll)
        ) {
            // ITERAMOS POR RONDAS
            rounds.forEach { (roundNum, roundMatches) ->

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "RONDA $roundNum",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
                    )

                    roundMatches.forEach { match ->
                        val p1 = players.find { it.id == match.player1Id }
                        val p2 = players.find { it.id == match.player2Id }

                        val spacerHeight = (40 * roundNum).dp

                        // USAMOS EL COMPONENTE CLICABLE
                        BracketMatchCard(
                            match = match,
                            p1 = p1,
                            p2 = p2,
                            onP1Click = {
                                if (isCreator && match.winnerId == null) {
                                    val nuevoScore = match.player1Score + 1
                                    val esGanador = nuevoScore >= match.maxScore

                                    val actualizado = match.copy(
                                        player1Score = nuevoScore,
                                        winnerId = if (esGanador) match.player1Id else null
                                    )
                                    onMatchUpdate(actualizado)
                                    if (esGanador) {
                                        android.widget.Toast.makeText(context, "¡${p1?.name} GANA!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onP2Click = {
                                if (isCreator && match.winnerId == null) {
                                    val nuevoScore = match.player2Score + 1
                                    val esGanador = nuevoScore >= match.maxScore

                                    val actualizado = match.copy(
                                        player2Score = nuevoScore,
                                        winnerId = if (esGanador) match.player2Id else null
                                    )
                                    onMatchUpdate(actualizado)
                                    if (esGanador) {
                                        android.widget.Toast.makeText(context, "¡${p2?.name} GANA!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(spacerHeight))
                    }
                }

                Spacer(modifier = Modifier.width(50.dp))
            }
        }
    }
}

// ==========================================
// COMPONENTES AUXILIARES Y DIÁLOGOS
// ==========================================

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 10.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun AddPlayerDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Añadir Participante", color = Color.White) },
        text = {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nombre / Gamertag") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFFD32F2F)
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) { Text("AÑADIR") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}

@Composable
fun StartTournamentDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var selectedFormat by remember { mutableStateOf(2) } // 2 = Bo3

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Configurar Partidas", color = Color.White) },
        text = {
            Column {
                Text("Formato de victoria:", color = Color.LightGray, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))

                // Opción Bo3
                Row(
                    Modifier.fillMaxWidth().clickable { selectedFormat = 2 }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedFormat == 2,
                        onClick = { selectedFormat = 2 },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F), unselectedColor = Color.Gray)
                    )
                    Text("Best of 3 (Primero a 2)", color = Color.White)
                }

                // Opción Bo5
                Row(
                    Modifier.fillMaxWidth().clickable { selectedFormat = 3 }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedFormat == 3,
                        onClick = { selectedFormat = 3 },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F), unselectedColor = Color.Gray)
                    )
                    Text("Best of 5 (Primero a 3)", color = Color.White)
                }

                // Opción Ft5
                Row(
                    Modifier.fillMaxWidth().clickable { selectedFormat = 5 }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedFormat == 5,
                        onClick = { selectedFormat = 5 },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F), unselectedColor = Color.Gray)
                    )
                    Text("First to 5 (Primero a 5)", color = Color.White)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedFormat) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Text("GENERAR BRACKET") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}

@Composable
fun EditTournamentDialog(
    currentName: String,
    currentDesc: String,
    currentDate: String,
    currentMax: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var desc by remember { mutableStateOf(currentDesc) }
    var date by remember { mutableStateOf(currentDate) }
    var maxPlayers by remember { mutableStateOf(currentMax.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Editar Torneo", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, label = { Text("Nombre") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray)
                )
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it }, label = { Text("Descripción") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray)
                )
                OutlinedTextField(
                    value = date, onValueChange = { date = it }, label = { Text("Fecha") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray)
                )
                OutlinedTextField(
                    value = maxPlayers, onValueChange = { maxPlayers = it }, label = { Text("Max Jugadores") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, desc, date, maxPlayers.toIntOrNull() ?: 16) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                Text("GUARDAR")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) } }
    )
}