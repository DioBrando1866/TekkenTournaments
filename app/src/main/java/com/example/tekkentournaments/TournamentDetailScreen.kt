package com.example.tekkentournaments

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.repositories.TournamentRepository
import com.example.tekkentournaments.repositories.UserRepository
import com.example.tekkentournaments.utils.TekkenData

// ==========================================
// PANTALLA PRINCIPAL (SCAFFOLDING)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournamentId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ESTADOS DE DATOS
    var tournament by remember { mutableStateOf<Tournament?>(null) }
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var participants by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // ESTADO PARA EL DIÁLOGO DE EDICIÓN
    var selectedMatchForEdit by remember { mutableStateOf<Match?>(null) }

    // CARGA INICIAL DE DATOS
    LaunchedEffect(tournamentId) {
        isLoading = true
        // 1. Cargar Torneo
        tournament = TournamentRepository.obtenerTorneoPorId(tournamentId)
        // 2. Cargar Partidos
        matches = TournamentRepository.obtenerPartidosDelTorneo(tournamentId)
        // 3. Cargar Usuarios
        participants = UserRepository.obtenerUsuarios()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tournament?.name ?: "Cargando...", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Bracket View", color = Color.Gray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFFD32F2F), modifier = Modifier.align(Alignment.Center))
            } else {
                // AQUÍ LLAMAMOS A TU COMPONENTE BRACKET
                TournamentBracket(
                    matches = matches,
                    users = participants,
                    onMatchClick = { match ->
                        selectedMatchForEdit = match
                    }
                )
            }
        }
    }

    // ==========================================
    // DIÁLOGO DE EDICIÓN (ZOOM IN LÓGICO)
    // ==========================================
    if (selectedMatchForEdit != null) {
        val match = selectedMatchForEdit!!
        val p1 = participants.find { it.id == match.player1Id }
        val p2 = participants.find { it.id == match.player2Id }

        // Estados locales para editar puntuación
        var score1 by remember { mutableStateOf(match.score1.toString()) }
        var score2 by remember { mutableStateOf(match.score2.toString()) }

        AlertDialog(
            onDismissRequest = { selectedMatchForEdit = null },
            containerColor = Color(0xFF1E1E1E),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ronda ${match.round}", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    if (match.winnerId != null) {
                        Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFD700))
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // --- ENCABEZADO IA ---
                    if (match.winnerId == null) {
                        Surface(
                            color = Color(0xFF00E5FF).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.SmartToy, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                // Lógica Fake de predicción
                                val p1Chance = if ((p1?.wins ?: 0) > (p2?.wins ?: 0)) 70 else 45
                                Text(
                                    "AI Prediction: ${if(p1Chance > 50) p1?.username else p2?.username} ($p1Chance%)",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // --- MARCADOR ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // PLAYER 1
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            AsyncImage(
                                model = p1?.profileImage,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(p1?.username ?: "P1", color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = score1,
                                onValueChange = { if (it.length <= 1) score1 = it.filter { c -> c.isDigit() } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(60.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray),
                                singleLine = true
                            )
                        }

                        Text("VS", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 8.dp))

                        // PLAYER 2
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            AsyncImage(
                                model = p2?.profileImage,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(p2?.username ?: "P2", color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = score2,
                                onValueChange = { if (it.length <= 1) score2 = it.filter { c -> c.isDigit() } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(60.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray),
                                singleLine = true
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    onClick = {
                        scope.launch {
                            val s1 = score1.toIntOrNull() ?: 0
                            val s2 = score2.toIntOrNull() ?: 0

                            // Validar ganador
                            var winnerId: String? = null
                            if (s1 > s2) winnerId = match.player1Id
                            else if (s2 > s1) winnerId = match.player2Id

                            // Guardar en BD
                            TournamentRepository.actualizarPartido(match.id, s1, s2, winnerId)

                            // Recargar y cerrar
                            matches = TournamentRepository.obtenerPartidosDelTorneo(tournamentId)
                            selectedMatchForEdit = null
                            Toast.makeText(context, "Resultado actualizado", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("GUARDAR RESULTADO")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMatchForEdit = null }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }
}


// =========================================================================
//  COMPONENTES DEL BRACKET (VISUALIZACIÓN)
// =========================================================================

// CONFIGURACIÓN VISUAL
private val CARD_WIDTH = 220.dp
private val CARD_HEIGHT = 90.dp
private val COLUMN_GAP = 80.dp // Espacio horizontal entre rondas
private val ROW_GAP = 20.dp    // Espacio vertical entre partidos

@Composable
fun TournamentBracket(
    matches: List<Match>,
    users: List<User>,
    onMatchClick: (Match) -> Unit
) {
    // ESTADOS PARA EL ZOOM Y PAN (MOVIMIENTO)
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Organizar partidos por rondas (Round 1, Round 2, Final...)
    val rounds = remember(matches) { matches.groupBy { it.round }.toSortedMap() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Fondo muy oscuro
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f) // Límites de zoom
                    offset += pan
                }
            }
    ) {
        // LIENZO APLICANDO LAS TRANSFORMACIONES
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            if (rounds.isNotEmpty()) {
                DrawConnections(rounds) // 1. Dibujamos las líneas primero (al fondo)
                DrawMatches(rounds, users, onMatchClick) // 2. Dibujamos las tarjetas encima
            } else {
                Text("Generando Bracket...", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            }
        }

        // Botón para resetear vista (útil si te pierdes haciendo zoom)
        FloatingActionButton(
            onClick = {
                scale = 1f; offset = Offset.Zero
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Color(0xFFD32F2F)
        ) {
            Text("RESET VISTA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}

@Composable
fun DrawMatches(
    rounds: Map<Int, List<Match>>,
    users: List<User>,
    onMatchClick: (Match) -> Unit
) {
    // Iteramos por columnas (Rondas)
    rounds.forEach { (roundNum, roundMatches) ->
        val xOffset = (roundNum - 1) * (CARD_WIDTH.value + COLUMN_GAP.value) // Posición X

        roundMatches.forEachIndexed { index, match ->
            // Lógica de posicionamiento vertical para árbol binario
            val verticalSpacing = (CARD_HEIGHT.value + ROW_GAP.value) * Math.pow(2.0, (roundNum - 1).toDouble()).toFloat()
            val startOffset = (verticalSpacing / 2) - (CARD_HEIGHT.value / 2) // Centrado inicial
            val yOffset = startOffset + (index * verticalSpacing)

            // DIBUJAR TARJETA EN POSICIÓN ABSOLUTA
            Box(
                modifier = Modifier
                    .offset(x = xOffset.dp, y = yOffset.dp)
                    .width(CARD_WIDTH)
                    .height(CARD_HEIGHT)
            ) {
                BracketMatchCard(match, users, onMatchClick)
            }
        }
    }
}

@Composable
fun DrawConnections(rounds: Map<Int, List<Match>>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        rounds.forEach { (roundNum, roundMatches) ->
            // No dibujamos líneas desde la última ronda (Final)
            if (roundNum < rounds.keys.maxOrNull()!!) {

                val currentX = (roundNum - 1) * (CARD_WIDTH.toPx() + COLUMN_GAP.toPx()) + CARD_WIDTH.toPx()
                val nextX = currentX + COLUMN_GAP.toPx()

                val currentSpacing = (CARD_HEIGHT.toPx() + ROW_GAP.toPx()) * Math.pow(2.0, (roundNum - 1).toDouble()).toFloat()
                val currentStartY = (currentSpacing / 2)

                // Dibujamos líneas desde cada partido de esta ronda hacia su "padre" en la siguiente
                roundMatches.forEachIndexed { index, _ ->
                    val startY = currentStartY + (index * currentSpacing)

                    val nextRoundIndex = index / 2
                    val nextSpacing = (CARD_HEIGHT.toPx() + ROW_GAP.toPx()) * Math.pow(2.0, roundNum.toDouble()).toFloat()
                    val nextStartY = (nextSpacing / 2)
                    val targetY = nextStartY + (nextRoundIndex * nextSpacing)

                    // Curva Bezier
                    val path = Path().apply {
                        moveTo(currentX, startY)
                        cubicTo(
                            currentX + COLUMN_GAP.toPx() / 2, startY, // Punto control 1
                            currentX + COLUMN_GAP.toPx() / 2, targetY, // Punto control 2
                            nextX, targetY // Destino
                        )
                    }

                    drawPath(
                        path = path,
                        color = Color.Gray.copy(alpha = 0.4f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun BracketMatchCard(match: Match, users: List<User>, onClick: (Match) -> Unit) {
    val p1 = users.find { it.id == match.player1Id }
    val p2 = users.find { it.id == match.player2Id }

    // Color del borde: Dorado si es final, Rojo normal, Gris si no está listo
    val borderColor = if (match.round >= 3) Color(0xFFFFD700) else if (match.winnerId != null) Color.Gray else Color(0xFFD32F2F)

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, borderColor),
        onClick = { onClick(match) }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // JUGADOR 1
            BracketPlayerRow(p1, match.score1, match.winnerId == match.player1Id)

            // SEPARADOR + INFO EXTRA (IA / Ronda)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(Color.Black.copy(alpha = 0.3f)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (match.winnerId == null) {
                    Icon(Icons.Rounded.SmartToy, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("AI PREDICTION", color = Color(0xFF00E5FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("FINALIZADO", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // JUGADOR 2
            BracketPlayerRow(p2, match.score2, match.winnerId == match.player2Id)
        }
    }
}

@Composable
fun ColumnScope.BracketPlayerRow(user: User?, score: Int, isWinner: Boolean) {
    val bgColor = if (isWinner) Color(0xFF2E7D32).copy(alpha = 0.3f) else Color.Transparent

    // Obtenemos la imagen del personaje (Main)
    val charImage = remember(user?.characterMain) {
        if (user?.characterMain != null) TekkenData.getCharacterImageUrl(user.characterMain) else null
    }

    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. IMAGEN DEL PERSONAJE (Círculo pequeño)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray)
        ) {
            if (charImage != null && charImage.isNotEmpty()) {
                AsyncImage(model = charImage, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                AsyncImage(model = user?.profileImage, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }

        Spacer(Modifier.width(8.dp))

        // 2. NOMBRE
        Text(
            text = user?.username ?: "TBD",
            color = if (isWinner) Color(0xFF4CAF50) else Color.White,
            fontSize = 10.sp,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // 3. PUNTUACIÓN
        Text(
            text = score.toString(),
            color = if (isWinner) Color(0xFF4CAF50) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}