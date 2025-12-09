package com.example.tekkentournaments.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology // Icono del cerebro
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    onAIClick: () -> Unit // <--- ¡AQUÍ ESTABA EL ERROR! Faltaba este parámetro
) {
    // Usamos Box para poder superponer el botón de IA sobre la tarjeta
    Box(contentAlignment = Alignment.TopEnd) {

        Card(
            modifier = Modifier
                .width(180.dp)
                .height(86.dp)
                .border(
                    width = 1.dp,
                    color = if (match.winnerId != null) Color(0xFF4CAF50) else Color(0xFF333333),
                    shape = RoundedCornerShape(8.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                PlayerRow(
                    name = p1?.name ?: "Esperando...",
                    character = p1?.characterMain,
                    score = match.player1Score,
                    isWinner = (match.winnerId != null && match.winnerId == match.player1Id),
                    onClick = onP1Click
                )

                Divider(color = Color(0xFF333333), thickness = 1.dp)

                PlayerRow(
                    name = p2?.name ?: "Esperando...",
                    character = p2?.characterMain,
                    score = match.player2Score,
                    isWinner = (match.winnerId != null && match.winnerId == match.player2Id),
                    onClick = onP2Click
                )
            }
        }

        // --- BOTÓN DE IA (CEREBRO) ---
        // Solo lo mostramos si los jugadores y personajes están listos
        if (p1 != null && p2 != null &&
            p1.characterMain != "Random" && p2.characterMain != "Random" &&
            match.winnerId == null) { // Y si el match no ha terminado

            IconButton(
                onClick = onAIClick,
                modifier = Modifier
                    .offset(x = 10.dp, y = (-10).dp) // Lo sacamos un poco por la esquina
                    .size(28.dp)
                    .background(Color(0xFF00E5FF), CircleShape) // Fondo Cyan
                    .border(1.dp, Color.Black, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Analizar con IA",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
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
            .height(42.dp)
            .clickable { onClick() }
            .background(if (isWinner) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Transparent)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = if (isWinner) Color(0xFF4CAF50) else Color.White,
                fontSize = 13.sp,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1
            )

            if (character != null && character != "Random") {
                Text(
                    text = character.uppercase(),
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 10.sp
                )
            }
        }

        Surface(
            color = if (isWinner) Color(0xFF4CAF50) else Color(0xFF333333),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = score.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}