package com.example.tekkentournaments

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.filled.Close

// IMPORTS DE CLASES Y REPOSITORIOS
import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.clases.Player
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.clases.AIService // Asegúrate de tener AIService.kt creado
import com.example.tekkentournaments.repositories.TournamentRepository
import com.example.tekkentournaments.ui.components.BracketMatchCard
import com.example.tekkentournaments.utils.TekkenData // Lista de personajes
import io.github.jan.supabase.auth.auth
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

    // --- ESTADOS PARA LA IA ---
    var showAIDialog by remember { mutableStateOf(false) }
    var aiMatchData by remember { mutableStateOf<Match?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // FUNCIÓN DE CARGA DE DATOS
    fun cargarDatos() {
        scope.launch {
            isLoading = true
            val t = TournamentRepository.obtenerTorneoPorId(tournamentId)
            tournament = t

            if (t != null) {
                val currentUser = supabase.auth.currentUserOrNull()
                isCreator = (currentUser != null && t.creatorId == currentUser.id)
                players = TournamentRepository.obtenerJugadores(tournamentId)
                matches = TournamentRepository.obtenerMatches(tournamentId)
            }
            isLoading = false
        }
    }

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
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("INFO") }, unselectedContentColor = Color.Gray)
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("JUGADORES") }, unselectedContentColor = Color.Gray)
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("BRACKET") }, unselectedContentColor = Color.Gray)
                }
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón Empezar (Verde)
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

                // Botón Añadir (Rojo)
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFD32F2F))
            } else if (tournament != null) {
                when (selectedTab) {
                    0 -> InfoTab(tournament!!)
                    1 -> PlayersTab(players)
                    2 -> BracketTab(
                        matches = matches,
                        players = players,
                        isCreator = isCreator,
                        onMatchUpdate = { matchActualizado ->
                            scope.launch {
                                TournamentRepository.actualizarPartida(matchActualizado)
                                cargarDatos()
                            }
                        },
                        onAIClick = { matchParaAnalizar ->
                            aiMatchData = matchParaAnalizar
                            showAIDialog = true
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
            // ✅ AÑADIDO: Pasamos la versión del juego del torneo actual
            // Si por algún motivo es null, ponemos "Tekken 8" por defecto
            gameVersion = tournament?.gameVersion ?: "Tekken 8",

            onDismiss = { showAddPlayerDialog = false },
            onConfirm = { name, character ->
                scope.launch {
                    TournamentRepository.agregarJugador(tournamentId, name, character)
                    cargarDatos()
                    showAddPlayerDialog = false
                }
            }
        )
    }

    // 2. EMPEZAR TORNEO
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
                        android.widget.Toast.makeText(context, "Error al generar bracket.", android.widget.Toast.LENGTH_LONG).show()
                    }
                    showStartDialog = false
                }
            }
        )
    }

    if (showAIDialog && aiMatchData != null) {
        val m = aiMatchData!!
        val p1 = players.find { it.id == m.player1Id }
        val p2 = players.find { it.id == m.player2Id }

        if (p1 != null && p2 != null) {
            TacticalAdviceDialog(
                p1Name = p1.name,
                char1 = p1.characterMain,
                p2Name = p2.name,
                char2 = p2.characterMain,

                // ✅ AÑADIDA ESTA LÍNEA:
                // Pasamos la versión del juego. Si aún no ha cargado, por defecto "Tekken 8"
                gameVersion = tournament?.gameVersion ?: "Tekken 8",

                onDismiss = { showAIDialog = false }
            )
        }
    }

    // 4. BORRAR TORNEO
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
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("CANCELAR", color = Color.Gray) } }
        )
    }

    // 5. EDITAR TORNEO
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
    Column(modifier = Modifier.padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("DESCRIPCIÓN", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(tournament.description ?: "Sin descripción.", color = Color.White, lineHeight = 22.sp)
        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {

                // 1. AHORA MUESTRA EL JUEGO REAL (Tekken 3, 7, 8...)
                InfoRow(Icons.Default.VideogameAsset, "Juego", tournament.gameVersion ?: "Tekken 8")

                Divider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 12.dp))

                // 2. AQUÍ ESTABA EL ERROR: Añadimos '?: "Estándar"'
                InfoRow(Icons.Default.EmojiEvents, "Formato", tournament.tournamentType ?: "Estándar")

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
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay jugadores inscritos.", color = Color.Gray) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(players) { player ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = Color.Gray)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(player.name, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Main: ${player.characterMain}", color = Color(0xFFD32F2F), fontSize = 12.sp)
                        }
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
    onMatchUpdate: (Match) -> Unit,
    onAIClick: (Match) -> Unit // Callback para la IA
) {
    if (matches.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("El torneo no ha comenzado.", color = Color.Gray) }
        return
    }

    val context = LocalContext.current
    val rounds = matches.groupBy { it.round }.toSortedMap()
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    Box(Modifier.fillMaxSize().background(Color(0xFF121212))) {
        Row(modifier = Modifier.padding(24.dp).horizontalScroll(horizontalScroll).verticalScroll(verticalScroll)) {
            rounds.forEach { (roundNum, roundMatches) ->
                Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    Text("RONDA $roundNum", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally))

                    roundMatches.forEach { match ->
                        val p1 = players.find { it.id == match.player1Id }
                        val p2 = players.find { it.id == match.player2Id }

                        BracketMatchCard(
                            match = match,
                            p1 = p1,
                            p2 = p2,
                            onP1Click = {
                                if (isCreator && match.winnerId == null) {
                                    val nuevoScore = match.player1Score + 1
                                    val esGanador = nuevoScore >= match.maxScore
                                    val actualizado = match.copy(player1Score = nuevoScore, winnerId = if (esGanador) match.player1Id else null)
                                    onMatchUpdate(actualizado)
                                    if (esGanador) Toast.makeText(context, "¡${p1?.name} GANA!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onP2Click = {
                                if (isCreator && match.winnerId == null) {
                                    val nuevoScore = match.player2Score + 1
                                    val esGanador = nuevoScore >= match.maxScore
                                    val actualizado = match.copy(player2Score = nuevoScore, winnerId = if (esGanador) match.player2Id else null)
                                    onMatchUpdate(actualizado)
                                    if (esGanador) Toast.makeText(context, "¡${p2?.name} GANA!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onAIClick = { onAIClick(match) } // Conectamos el botón IA
                        )
                        Spacer(modifier = Modifier.height((40 * roundNum).dp))
                    }
                }
                Spacer(modifier = Modifier.width(50.dp))
            }
        }
    }
}

// ==========================================
// DIÁLOGOS DE APLICACIÓN
// ==========================================

@Composable
fun TacticalAdviceDialog(
    p1Name: String,
    char1: String,
    p2Name: String,
    char2: String,
    gameVersion: String,
    onDismiss: () -> Unit
) {
    // Estado: ¿Quién soy yo? (true = Soy P1, false = Soy P2)
    var isPlayer1Perspective by remember { mutableStateOf(true) }

    var advice by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Nombres para mostrar en la UI
    val mainChar = if (isPlayer1Perspective) char1 else char2
    val rivalChar = if (isPlayer1Perspective) char2 else char1

    // Efecto de carga
    LaunchedEffect(isPlayer1Perspective) {
        isLoading = true
        advice = ""
        // Llamada a la IA
        advice = AIService.obtenerConsejoTactico(
            miPersonaje = mainChar,
            rivalPersonaje = rivalChar,
            juego = gameVersion
        )
        isLoading = false
    }

    // USAMOS 'Dialog' EN LUGAR DE 'AlertDialog' PARA TENER CONTROL TOTAL DEL TAMAÑO
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Permite ocupar ancho completo
    ) {
        // TARJETA PRINCIPAL (El contenedor grande)
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f) // 95% del ancho de la pantalla
                .fillMaxHeight(0.90f) // 90% del alto (casi pantalla completa)
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp)), // Borde Cyberpunk
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)) // Fondo muy oscuro
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // --- CABECERA ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, null, tint = Color(0xFF00E5FF))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "FRAME DATA & STRATEGY",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                }

                Divider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 12.dp))

                // --- SELECTOR DE PERSPECTIVA (PESTAÑAS GRANDES) ---
                Text("ANALIZAR DESDE LA VISTA DE:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp) // Más grandes para tocar fácil
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                ) {
                    // Pestaña Izquierda
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isPlayer1Perspective) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                            )
                            .clickable { isPlayer1Perspective = true }
                    ) {
                        Text(
                            text = char1.uppercase(),
                            color = if (isPlayer1Perspective) Color(0xFF00E5FF) else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if(isPlayer1Perspective) {
                            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp).background(Color(0xFF00E5FF)))
                        }
                    }

                    // Línea divisoria vertical
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFF333333)))

                    // Pestaña Derecha
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (!isPlayer1Perspective) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                            )
                            .clickable { isPlayer1Perspective = false }
                    ) {
                        Text(
                            text = char2.uppercase(),
                            color = if (!isPlayer1Perspective) Color(0xFF00E5FF) else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if(!isPlayer1Perspective) {
                            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp).background(Color(0xFF00E5FF)))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- ZONA DE CONTENIDO (CON SCROLL) ---
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Consultando base de datos...", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                } else {
                    // Título del Matchup
                    Text(
                        text = "VS ${rivalChar.uppercase()}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // CAJA DE TEXTO DEL CONSEJO
                    Box(
                        modifier = Modifier
                            .weight(1f) // Ocupa todo el espacio vertical restante
                            .fillMaxWidth()
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                            Text(
                                text = advice,
                                color = Color(0xFFEEEEEE),
                                fontSize = 15.sp, // Letra un poco más grande
                                lineHeight = 24.sp, // Más espacio entre líneas para leer bien
                                fontFamily = FontFamily.Monospace // Fuente tipo "Hacker" para datos técnicos
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTÓN CERRAR INFERIOR
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ENTENDIDO", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlayerDialog(
    gameVersion: String, // <--- Importante: Recibimos qué juego es
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCharacter by remember { mutableStateOf("Random") }

    // Obtenemos la lista correcta usando tu nuevo TekkenData
    val characterList = remember(gameVersion) {
        TekkenData.getCharacters(gameVersion)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Nuevo Participante ($gameVersion)", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Input Nombre
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Gamertag") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Input Personaje (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCharacter,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Personaje Main") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFFD32F2F)
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF2C2C2C))
                    ) {
                        // AQUÍ ES DONDE USAMOS LA NUEVA LISTA
                        characterList.forEach { charName ->
                            DropdownMenuItem(
                                text = { Text(charName, color = Color.White) },
                                onClick = {
                                    selectedCharacter = charName
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, selectedCharacter) },
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
    var selectedFormat by remember { mutableStateOf(2) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Configurar Partidas", color = Color.White) },
        text = {
            Column {
                Text("Formato de victoria:", color = Color.LightGray, fontSize = 14.sp); Spacer(Modifier.height(16.dp))
                listOf(2 to "Best of 3 (Primero a 2)", 3 to "Best of 5 (Primero a 3)", 5 to "First to 5 (Primero a 5)").forEach { (valFormat, label) ->
                    Row(Modifier.fillMaxWidth().clickable { selectedFormat = valFormat }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedFormat == valFormat, onClick = { selectedFormat = valFormat }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F), unselectedColor = Color.Gray))
                        Text(label, color = Color.White)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selectedFormat) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("GENERAR BRACKET") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) } }
    )
}

@Composable
fun EditTournamentDialog(currentName: String, currentDesc: String, currentDate: String, currentMax: Int, onDismiss: () -> Unit, onConfirm: (String, String, String, Int) -> Unit) {
    var name by remember { mutableStateOf(currentName) }; var desc by remember { mutableStateOf(currentDesc) }
    var date by remember { mutableStateOf(currentDate) }; var maxPlayers by remember { mutableStateOf(currentMax.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFF1E1E1E), title = { Text("Editar Torneo", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Fecha") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = maxPlayers, onValueChange = { maxPlayers = it }, label = { Text("Max Jugadores") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            }
        },
        confirmButton = { Button(onClick = { onConfirm(name, desc, date, maxPlayers.toIntOrNull() ?: 16) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("GUARDAR") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) } }
    )
}