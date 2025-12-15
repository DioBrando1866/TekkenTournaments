package com.example.tekkentournaments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// --- IMPORTS ---
import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.clases.Player
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.clases.AIService
import com.example.tekkentournaments.repositories.TournamentRepository
import com.example.tekkentournaments.repositories.UserRepository
import com.example.tekkentournaments.viewmodel.TournamentViewModel
import com.example.tekkentournaments.utils.TekkenData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournamentId: String,
    onBack: () -> Unit,
    onTournamentDeleted: () -> Unit,
    viewModel: TournamentViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()

    // Estados de Datos
    val matches by viewModel.matches.collectAsState()
    val isMatchesLoading by viewModel.isLoading.collectAsState()

    var tournament by remember { mutableStateOf<Tournament?>(null) }
    var playersList by remember { mutableStateOf<List<Player>>(emptyList()) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isLoadingInfo by remember { mutableStateOf(true) }

    // Estados de UI
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Info", "Jugadores", "Bracket")
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedMatchForDetails by remember { mutableStateOf<Match?>(null) }

    // Carga Inicial
    LaunchedEffect(tournamentId) {
        isLoadingInfo = true
        tournament = TournamentRepository.obtenerTorneoPorId(tournamentId)
        playersList = TournamentRepository.obtenerJugadores(tournamentId)
        currentUser = UserRepository.obtenerMiPerfil()
        viewModel.loadMatches(tournamentId)
        isLoadingInfo = false
    }

    val isCreator = remember(tournament, currentUser) {
        tournament != null && currentUser != null && tournament?.creatorId == currentUser?.id
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tournament?.name ?: "Cargando...", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(tournament?.gameVersion ?: "", color = Color.Gray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás", tint = Color.White) }
                },
                actions = {
                    if (isCreator) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFD32F2F))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        containerColor = Color(0xFF0A0A0A),
        floatingActionButton = {
            if (selectedTabIndex == 1) {
                FloatingActionButton(onClick = { showAddPlayerDialog = true }, containerColor = Color(0xFFD32F2F)) {
                    Icon(Icons.Default.Add, "Añadir", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color(0xFFD32F2F),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = Color(0xFFD32F2F))
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, color = if (selectedTabIndex == index) Color.White else Color.Gray) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoadingInfo) {
                    CircularProgressIndicator(color = Color(0xFFD32F2F), modifier = Modifier.align(Alignment.Center))
                } else {
                    when (selectedTabIndex) {
                        0 -> InfoTab(tournament, playersList.size, isCreator) { showDeleteDialog = true }
                        1 -> PlayersTab(playersList)
                        2 -> {
                            if (isMatchesLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            } else if (matches.isNotEmpty()) {
                                TournamentBracketGraph(
                                    matches = matches,
                                    players = playersList,
                                    onMatchClick = { selectedMatchForDetails = it }
                                )
                            } else {
                                EmptyBracketView(isCreator, playersList.size) {
                                    scope.launch {
                                        // Por defecto BO3 (3 juegos máx)
                                        TournamentRepository.generarBracketInicial(tournamentId, 3)
                                        viewModel.loadMatches(tournamentId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---
    if (showAddPlayerDialog) {
        AddPlayerDialog(
            onDismiss = { showAddPlayerDialog = false },
            onAdd = { name, char ->
                scope.launch {
                    TournamentRepository.agregarJugador(tournamentId, name, char)
                    playersList = TournamentRepository.obtenerJugadores(tournamentId)
                    showAddPlayerDialog = false
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1E1E1E),
            icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFD32F2F)) },
            title = { Text("¿Eliminar Torneo?", color = Color.White) },
            text = { Text("Esta acción es irreversible.", color = Color.LightGray) },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        TournamentRepository.eliminarTorneo(tournamentId)
                        showDeleteDialog = false
                        onTournamentDeleted()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = Color.White) } }
        )
    }

    if (selectedMatchForDetails != null) {
        val match = selectedMatchForDetails!!
        val p1 = playersList.find { it.id == match.player1Id }
        val p2 = playersList.find { it.id == match.player2Id }

        // Calculamos el título de este match específico para mostrarlo en el diálogo
        val totalRounds = matches.maxOfOrNull { it.round } ?: 1
        val matchTitle = getRoundTitle(match.round, totalRounds)

        MatchDetailDialog(
            match = match,
            title = matchTitle, // Pasamos el título corregido
            p1Name = p1?.name ?: "P1",
            p2Name = p2?.name ?: "P2",
            p1Char = p1?.characterMain ?: "Random",
            p2Char = p2?.characterMain ?: "Random",
            gameVersion = tournament?.gameVersion ?: "Tekken 8",
            isCreator = isCreator,
            onDismiss = { selectedMatchForDetails = null },
            onSaveResult = { s1, s2 ->
                viewModel.reportMatchResult(match.id, s1, s2)
                selectedMatchForDetails = null
            }
        )
    }
}

// ==========================================
// LÓGICA DE NOMBRES DE RONDAS (TEKKEN STYLE)
// ==========================================
fun getRoundTitle(round: Int, maxRound: Int): String {
    return when (round) {
        maxRound -> "Grand Finals (FT3)" // Best of 5
        maxRound - 1 -> "Winners Finals (FT3)" // Best of 5
        maxRound - 2 -> "Winners Semis (FT2)" // Best of 3
        else -> "Pools - Ronda $round (FT2)" // Best of 3
    }
}

// ==========================================
// COMPONENTES UI
// ==========================================

@Composable
fun TournamentBracketGraph(matches: List<Match>, players: List<Player>, onMatchClick: (Match) -> Unit) {
    val rounds = matches.groupBy { it.round }
    // Calculamos la ronda máxima para saber cuál es la final
    val maxRound = rounds.keys.maxOrNull() ?: 1

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ -> scale = (scale * zoom).coerceIn(0.5f, 3f); offset += pan }
    }) {
        Row(modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y).padding(32.dp)) {

            for (r in 1..maxRound) {
                val matchesInRound = rounds[r] ?: emptyList()
                // Obtenemos el título dinámico
                val roundTitle = getRoundTitle(r, maxRound)

                // Color diferente para Finals
                val headerColor = if (r >= maxRound - 1) Color(0xFFFFD700) else Color(0xFFD32F2F)

                Column(modifier = Modifier.width(240.dp).padding(end = 40.dp), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = roundTitle,
                        color = headerColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    matchesInRound.forEach { match ->
                        BracketCard(match, players, onMatchClick)
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BracketCard(match: Match, players: List<Player>, onClick: (Match) -> Unit) {
    val borderColor = if (match.winnerId != null) Color(0xFF4CAF50) else Color.Gray
    val p1Name = players.find { it.id == match.player1Id }?.name ?: "Bye"
    val p2Name = players.find { it.id == match.player2Id }?.name ?: "Bye"

    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable { onClick(match) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            // P1
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(p1Name, color = if(match.winnerId == match.player1Id && match.player1Id != null) Color(0xFF4CAF50) else Color.White, fontWeight = FontWeight.Bold)
                Text("${match.player1Score}", color = Color.White)
            }
            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
            // P2
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(p2Name, color = if(match.winnerId == match.player2Id && match.player2Id != null) Color(0xFF4CAF50) else Color.White, fontWeight = FontWeight.Bold)
                Text("${match.player2Score}", color = Color.White)
            }
        }
    }
}

// --- DIÁLOGO DETALLES ACTUALIZADO ---
@Composable
fun MatchDetailDialog(
    match: Match, title: String, // Recibe el título
    p1Name: String, p2Name: String, p1Char: String, p2Char: String,
    gameVersion: String, isCreator: Boolean, onDismiss: () -> Unit, onSaveResult: (Int, Int) -> Unit
) {
    var s1 by remember { mutableStateOf(match.player1Score.toString()) }
    var s2 by remember { mutableStateOf(match.player2Score.toString()) }
    var aiAdvice by remember { mutableStateOf("") }
    var isAiLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFF1E1E1E),
        title = {
            Column {
                Text(title, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Editar Resultado", color = Color.Gray, fontSize = 14.sp)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$p1Name ($p1Char)", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("VS", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                    Text("$p2Name ($p2Char)", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
                Spacer(Modifier.height(16.dp))

                if (p1Char != "Random" && p2Char != "Random") {
                    Button(
                        onClick = { scope.launch { isAiLoading = true; aiAdvice = AIService.obtenerConsejoTactico(p1Char, p2Char, gameVersion); isAiLoading = false } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)), modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isAiLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        else { Icon(Icons.Rounded.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("Pedir Consejo IA") }
                    }
                    if (aiAdvice.isNotEmpty()) Card(colors = CardDefaults.cardColors(containerColor = Color.Black), modifier = Modifier.padding(top = 8.dp)) {
                        Text(aiAdvice, color = Color(0xFFE1BEE7), fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                }

                if (isCreator) {
                    Row {
                        OutlinedTextField(value = s1, onValueChange = { s1 = it }, label = { Text("P1") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(value = s2, onValueChange = { s2 = it }, label = { Text("P2") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    }
                }
            }
        },
        confirmButton = {
            if (isCreator) Button(onClick = { onSaveResult(s1.toIntOrNull()?:0, s2.toIntOrNull()?:0) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("Guardar") }
            else TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

// --- OTROS COMPONENTES AUXILIARES ---
@Composable
fun InfoTab(tournament: Tournament?, playerCount: Int, isCreator: Boolean, onDeleteRequest: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
        Text(tournament?.name ?: "", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                InfoRow("Participantes", "$playerCount / ${tournament?.maxPlayers}")
                InfoRow("Juego", tournament?.gameVersion ?: "Desconocido")
                InfoRow("Formato", tournament?.tournamentType ?: "Estándar")
                InfoRow("Fecha", tournament?.date ?: "TBD")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isCreator) {
            OutlinedButton(onClick = onDeleteRequest, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)), border = BorderStroke(1.dp, Color(0xFFD32F2F))) {
                Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("ELIMINAR TORNEO", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", color = Color.Gray)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PlayersTab(players: List<Player>) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(players) { player ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = TekkenData.getCharacterImageUrl(player.characterMain), contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(16.dp))
                    Column { Text(player.name, color = Color.White, fontWeight = FontWeight.Bold); Text(player.characterMain, color = Color.Gray, fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable
fun EmptyBracketView(isCreator: Boolean, playerCount: Int, onGenerate: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bracket no generado", color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            if (isCreator) {
                if (playerCount >= 2) Button(onClick = onGenerate, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("Generar Bracket Inicial") }
                else Text("Se necesitan mín. 2 jugadores", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AddPlayerDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedChar by remember { mutableStateOf("Random") }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFF1E1E1E),
        title = { Text("Añadir Jugador", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.height(200.dp)) { CharacterGridSelector(gameVersion = "Tekken 8", selectedCharacter = selectedChar, onCharacterSelected = { selectedChar = it }) }
            }
        },
        confirmButton = { Button(onClick = { if (name.isNotEmpty()) onAdd(name, selectedChar) }) { Text("Añadir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}