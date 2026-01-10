package com.example.tekkentournaments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.utils.TekkenData

private val CARD_WIDTH = 220.dp
private val CARD_HEIGHT = 90.dp
private val COLUMN_GAP = 80.dp
private val ROW_GAP = 20.dp

@Composable
fun TournamentBracket(
    matches: List<Match>,
    users: List<User>,
    onMatchClick: (Match) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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
                DrawConnections(rounds)
                DrawMatches(rounds, users, onMatchClick)
            } else {
                Text("No hay partidos generados", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            }
        }

        FloatingActionButton(
            onClick = { scale = 1f; offset = Offset.Zero },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Color(0xFFD32F2F)
        ) {
            Text("RESET", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DrawMatches(
    rounds: Map<Int, List<Match>>,
    users: List<User>,
    onMatchClick: (Match) -> Unit
) {
    rounds.forEach { (roundNum, roundMatches) ->
        val xOffset = (roundNum - 1) * (CARD_WIDTH.value + COLUMN_GAP.value)

        roundMatches.forEachIndexed { index, match ->
            val verticalSpacing = (CARD_HEIGHT.value + ROW_GAP.value) * Math.pow(2.0, (roundNum - 1).toDouble()).toFloat()
            val startOffset = (verticalSpacing / 2) - (CARD_HEIGHT.value / 2)
            val yOffset = startOffset + (index * verticalSpacing)

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
            if (roundNum < rounds.keys.maxOrNull()!!) {

                val currentX = (roundNum - 1) * (CARD_WIDTH.toPx() + COLUMN_GAP.toPx()) + CARD_WIDTH.toPx()
                val nextX = currentX + COLUMN_GAP.toPx()

                val currentSpacing = (CARD_HEIGHT.toPx() + ROW_GAP.toPx()) * Math.pow(2.0, (roundNum - 1).toDouble()).toFloat()
                val currentStartY = (currentSpacing / 2)

                roundMatches.forEachIndexed { index, _ ->
                    val startY = currentStartY + (index * currentSpacing)

                    val nextRoundIndex = index / 2
                    val nextSpacing = (CARD_HEIGHT.toPx() + ROW_GAP.toPx()) * Math.pow(2.0, roundNum.toDouble()).toFloat()
                    val nextStartY = (nextSpacing / 2)
                    val targetY = nextStartY + (nextRoundIndex * nextSpacing)

                    val path = Path().apply {
                        moveTo(currentX, startY)
                        cubicTo(
                            currentX + COLUMN_GAP.toPx() / 2, startY,
                            currentX + COLUMN_GAP.toPx() / 2, targetY,
                            nextX, targetY
                        )
                    }

                    drawPath(
                        path = path,
                        color = Color.Gray.copy(alpha = 0.5f),
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

    val borderColor = if (match.round >= 3) Color(0xFFFFD700) else if (match.winnerId != null) Color.Gray else Color(0xFFD32F2F)

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, borderColor),
        onClick = { onClick(match) }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BracketPlayerRow(p1, match.player1Score, match.winnerId == match.player1Id)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(Color.Black.copy(alpha = 0.3f)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (match.winnerId == null) {
                    Icon(Icons.Rounded.SmartToy, null, tint = Color.Cyan, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("AI PREDICTION", color = Color.Cyan, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("FINALIZADO", color = Color.Gray, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                }
            }

            BracketPlayerRow(p2, match.player2Score, match.winnerId == match.player2Id)
        }
    }
}

@Composable
fun ColumnScope.BracketPlayerRow(user: User?, score: Int, isWinner: Boolean) {
    val bgColor = if (isWinner) Color(0xFF2E7D32).copy(alpha = 0.3f) else Color.Transparent

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

        Text(
            text = user?.username ?: "TBD",
            color = if (isWinner) Color(0xFF4CAF50) else Color.White,
            fontSize = 10.sp,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = score.toString(),
            color = if (isWinner) Color(0xFF4CAF50) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}