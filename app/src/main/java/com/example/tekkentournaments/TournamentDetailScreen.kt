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
import androidx.compose.ui.res.stringResource // <--- Importante
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.filled.Close

import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.clases.Player
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.clases.AIService
import com.example.tekkentournaments.repositories.TournamentRepository
import com.example.tekkentournaments.ui.components.BracketMatchCard
import com.example.tekkentournaments.CharacterGridSelector
import com.example.tekkentournaments.utils.TekkenData
import io.github.jan.supabase.auth.auth
import supabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournamentId: String,
    onBack: () -> Unit,
    onTournamentDeleted: () -> Unit
) {
    var tournament by remember { mutableStateOf<Tournament?>(null) }
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }

    var isCreator by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }

    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showStartDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    var showAIDialog by remember { mutableStateOf(false) }
    var aiMatchData by remember { mutableStateOf<Match?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                            tournament?.name?.uppercase() ?: stringResource(R.string.loading_tournament),
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
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = Color.White)
                        }
                    },
                    actions = {
                        if (isCreator && !isLoading) {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Default.Edit, stringResource(R.string.btn_edit), tint = Color.White)
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, stringResource(R.string.btn_delete), tint = Color(0xFFD32F2F))
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
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(stringResource(R.string.tab_info)) }, unselectedContentColor = Color.Gray)
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(stringResource(R.string.tab_players)) }, unselectedContentColor = Color.Gray)
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(stringResource(R.string.tab_bracket)) }, unselectedContentColor = Color.Gray)
                }
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isCreator && selectedTab == 1 && matches.isEmpty() && players.size >= 2) {
                    ExtendedFloatingActionButton(
                        onClick = { showStartDialog = true },
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_start))
                    }
                }

                if (isCreator && selectedTab == 1 && matches.isEmpty()) {
                    FloatingActionButton(
                        onClick = { showAddPlayerDialog = true },
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, stringResource(R.string.btn_add))
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

    if (showAddPlayerDialog) {
        AddPlayerDialog(
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

    if (showStartDialog) {
        StartTournamentDialog(
            onDismiss = { showStartDialog = false },
            onConfirm = { formato ->
                scope.launch {
                    val exito = TournamentRepository.generarBracketInicial(tournamentId, formato)
                    if (exito) {
                        android.widget.Toast.makeText(context, context.getString(R.string.bracket_generated_toast), android.widget.Toast.LENGTH_SHORT).show()
                        cargarDatos()
                        selectedTab = 2
                    } else {
                        android.widget.Toast.makeText(context, context.getString(R.string.bracket_error_toast), android.widget.Toast.LENGTH_LONG).show()
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
                gameVersion = tournament?.gameVersion ?: "Tekken 8",
                onDismiss = { showAIDialog = false }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text(stringResource(R.string.delete_tournament_title), color = Color.White) },
            text = { Text(stringResource(R.string.delete_tournament_msg), color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val exito = TournamentRepository.borrarTorneo(tournamentId)
                            if (exito) onTournamentDeleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text(stringResource(R.string.btn_delete_confirm)) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.exit), color = Color.Gray) } }
        )
    }

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
        Text(stringResource(R.string.description_label), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(tournament.description ?: stringResource(R.string.no_description), color = Color.White, lineHeight = 22.sp)
        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                InfoRow(Icons.Default.VideogameAsset, stringResource(R.string.game_label), tournament.gameVersion ?: "Tekken 8")
                Divider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(Icons.Default.EmojiEvents, stringResource(R.string.format_label), tournament.tournamentType ?: stringResource(R.string.format_default))
                Divider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(Icons.Default.CalendarToday, stringResource(R.string.date_label), tournament.date ?: stringResource(R.string.date_tbd))
                Divider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(Icons.Default.Group, stringResource(R.string.players_label), "${tournament.maxPlayers} ${stringResource(R.string.players_max)}")
            }
        }
    }
}

@Composable
fun PlayersTab(players: List<Player>) {
    if (players.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.no_players_registered), color = Color.Gray) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(players) { player ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = Color.Gray)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(player.name, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${stringResource(R.string.main_char_label)} ${player.characterMain}", color = Color(0xFFD32F2F), fontSize = 12.sp)
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
    onAIClick: (Match) -> Unit
) {
    if (matches.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.tournament_not_started), color = Color.Gray) }
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
                    Text("${stringResource(R.string.round_label)} $roundNum", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally))

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
                                    if (esGanador) Toast.makeText(context, context.getString(R.string.winner_toast, p1?.name), Toast.LENGTH_SHORT).show()
                                }
                            },
                            onP2Click = {
                                if (isCreator && match.winnerId == null) {
                                    val nuevoScore = match.player2Score + 1
                                    val esGanador = nuevoScore >= match.maxScore
                                    val actualizado = match.copy(player2Score = nuevoScore, winnerId = if (esGanador) match.player2Id else null)
                                    onMatchUpdate(actualizado)
                                    if (esGanador) Toast.makeText(context, context.getString(R.string.winner_toast, p2?.name), Toast.LENGTH_SHORT).show()
                                }
                            },
                            onAIClick = { onAIClick(match) }
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
    p1Name: String, char1: String, p2Name: String, char2: String, gameVersion: String, onDismiss: () -> Unit
) {
    var isPlayer1Perspective by remember { mutableStateOf(true) }
    var advice by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val mainChar = if (isPlayer1Perspective) char1 else char2
    val rivalChar = if (isPlayer1Perspective) char2 else char1

    LaunchedEffect(isPlayer1Perspective) {
        isLoading = true
        advice = ""
        advice = AIService.obtenerConsejoTactico(mainChar, rivalChar, gameVersion)
        isLoading = false
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.90f).border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, null, tint = Color(0xFF00E5FF))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.ai_dialog_title), color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
                }

                Divider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 12.dp))

                Text(stringResource(R.string.analyze_view_label), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth().height(50.dp).background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp)).border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxHeight().background(if (isPlayer1Perspective) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)).clickable { isPlayer1Perspective = true }) {
                        Text(char1.uppercase(), color = if (isPlayer1Perspective) Color(0xFF00E5FF) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if(isPlayer1Perspective) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp).background(Color(0xFF00E5FF)))
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFF333333)))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxHeight().background(if (!isPlayer1Perspective) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)).clickable { isPlayer1Perspective = false }) {
                        Text(char2.uppercase(), color = if (!isPlayer1Perspective) Color(0xFF00E5FF) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if(!isPlayer1Perspective) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp).background(Color(0xFF00E5FF)))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.consulting_db), color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                } else {
                    Text("${stringResource(R.string.vs_label)} ${rivalChar.uppercase()}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp)).padding(16.dp)) {
                        val scrollState = rememberScrollState()
                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                            Text(advice, color = Color(0xFFEEEEEE), fontSize = 15.sp, lineHeight = 24.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)), shape = RoundedCornerShape(8.dp)) {
                        Text(stringResource(R.string.btn_understood), color = Color.Black, fontWeight = FontWeight.Bold)
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
    gameVersion: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    // Ya no necesitamos 'expanded' porque no hay dropdown
    var selectedCharacter by remember { mutableStateOf("Random") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text(stringResource(R.string.new_participant_title, gameVersion), color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // 1. INPUT NOMBRE (Igual que antes)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.gamertag_label)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. NUEVO SELECTOR VISUAL (GRID)
                // Sustituye al ExposedDropdownMenuBox
                CharacterGridSelector(
                    gameVersion = gameVersion,
                    selectedCharacter = selectedCharacter,
                    onCharacterSelected = { charName ->
                        selectedCharacter = charName
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, selectedCharacter) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) { Text(stringResource(R.string.btn_add_player)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.exit), color = Color.Gray) }
        }
    )
}

@Composable
fun StartTournamentDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var selectedFormat by remember { mutableStateOf(2) }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFF1E1E1E), title = { Text(stringResource(R.string.config_matches_title), color = Color.White) },
        text = {
            Column {
                Text(stringResource(R.string.victory_format_label), color = Color.LightGray, fontSize = 14.sp); Spacer(Modifier.height(16.dp))
                listOf(2 to stringResource(R.string.bo3_label), 3 to stringResource(R.string.bo5_label), 5 to stringResource(R.string.ft5_label)).forEach { (valFormat, label) ->
                    Row(Modifier.fillMaxWidth().clickable { selectedFormat = valFormat }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedFormat == valFormat, onClick = { selectedFormat = valFormat }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F), unselectedColor = Color.Gray))
                        Text(label, color = Color.White)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selectedFormat) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text(stringResource(R.string.btn_generate_bracket)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.exit), color = Color.Gray) } }
    )
}

@Composable
fun EditTournamentDialog(currentName: String, currentDesc: String, currentDate: String, currentMax: Int, onDismiss: () -> Unit, onConfirm: (String, String, String, Int) -> Unit) {
    var name by remember { mutableStateOf(currentName) }; var desc by remember { mutableStateOf(currentDesc) }
    var date by remember { mutableStateOf(currentDate) }; var maxPlayers by remember { mutableStateOf(currentMax.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFF1E1E1E), title = { Text(stringResource(R.string.edit_tournament_title), color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.name_label)) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text(stringResource(R.string.description_label)) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text(stringResource(R.string.date_label)) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = maxPlayers, onValueChange = { maxPlayers = it }, label = { Text(stringResource(R.string.max_players_label)) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            }
        },
        confirmButton = { Button(onClick = { onConfirm(name, desc, date, maxPlayers.toIntOrNull() ?: 16) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text(stringResource(R.string.btn_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.exit), color = Color.Gray) } }
    )
}