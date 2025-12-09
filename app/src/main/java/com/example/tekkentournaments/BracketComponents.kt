package com.example.tekkentournaments.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.clases.Player

@Composable
fun BracketMatchCard(
    match: Match,
    p1: Player?,
    p2: Player?,
    onP1Click: () -> Unit,
    onP2Click: () -> Unit,
    onAIClick: () -> Unit
) {
    // Definimos si se puede usar la IA (Match válido y no terminado)
    val showAI = p1 != null && p2 != null &&
            p1.characterMain != "Random" && p2.characterMain != "Random" &&
            match.winnerId == null

    Card(
        modifier = Modifier
            .width(200.dp) // Un poco más ancho para que quepa la cabecera bien
            .wrapContentHeight(), // Altura dinámica
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = if (match.winnerId != null) BorderStroke(1.dp, Color(0xFF4CAF50)) else BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Column {

            // --- 1. CABECERA (HEADER) ---
            // Aquí ponemos el ID del match y el botón de la IA a la derecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp) // Altura fija para la cabecera
                    .background(Color(0xFF252525)) // Fondo ligeramente más claro
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Texto izquierda (ID o Ronda)
                Text(
                    text = "MATCH ${match.id.take(4).uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                // Botón Derecha (IA) - INTEGRADO, NO FLOTANTE
                if (showAI) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onAIClick() }
                            .padding(4.dp), // Área táctil
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IA TIPS",
                            color = Color(0xFF00E5FF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "IA",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Divider(color = Color(0xFF333333))

            // --- 2. JUGADORES ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                PlayerRow(
                    name = p1?.name ?: "Esperando...",
                    character = p1?.characterMain,
                    score = match.player1Score,
                    isWinner = (match.winnerId != null && match.winnerId == match.player1Id),
                    onClick = onP1Click
                )

                // Separador sutil entre jugadores
                Divider(
                    color = Color(0xFF333333),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                PlayerRow(
                    name = p2?.name ?: "Esperando...",
                    character = p2?.characterMain,
                    score = match.player2Score,
                    isWinner = (match.winnerId != null && match.winnerId == match.player2Id),
                    onClick = onP2Click
                )
            }
        }
    }
}

@Composable
fun PlayerRow(
    name: String,
    character: String?,
    score: Int,
    isWinner: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable { onClick() }
            .background(if (isWinner) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color.Transparent)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna Nombre y Personaje
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = if (isWinner) Color(0xFF4CAF50) else Color.White,
                fontSize = 13.sp,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (character != null && character != "Random") {
                Text(
                    text = character.uppercase(),
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 10.sp,
                    maxLines = 1
                )
            }
        }

        // Puntuación
        Surface(
            color = if (isWinner) Color(0xFF4CAF50) else Color(0xFF2C2C2C),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = score.toString(),
                    color = if (isWinner) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}