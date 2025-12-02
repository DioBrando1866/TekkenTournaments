package com.example.tekkentournaments.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.clases.Player

// En ui/components/BracketComponents.kt

@Composable
fun BracketMatchCard(
    match: Match,
    p1: Player?,
    p2: Player?,
    // Nuevos callbacks específicos
    onP1Click: () -> Unit,
    onP2Click: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp) // Un poco más ancha para que quepa todo
            .height(80.dp)
            .border(
                width = 1.dp,
                color = if (match.winnerId != null) Color(0xFF4CAF50) else Color(0xFF333333), // Borde verde si ya terminó
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // JUGADOR 1
            PlayerRow(
                name = p1?.name ?: "Esperando...",
                score = match.player1Score,
                isWinner = (match.winnerId != null && match.winnerId == match.player1Id),
                onClick = onP1Click // Conectamos el click
            )

            Divider(color = Color(0xFF333333), thickness = 1.dp)

            // JUGADOR 2
            PlayerRow(
                name = p2?.name ?: "Esperando...",
                score = match.player2Score,
                isWinner = (match.winnerId != null && match.winnerId == match.player2Id),
                onClick = onP2Click // Conectamos el click
            )
        }
    }
}

@Composable
fun PlayerRow(
    name: String,
    score: Int,
    isWinner: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(39.dp) // Mitad de la tarjeta
            .clickable { onClick() } // <--- AQUÍ ESTÁ LA MAGIA
            .background(if (isWinner) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Transparent) // Fondo verde sutil si ganó
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            color = if (isWinner) Color(0xFF4CAF50) else Color.White,
            fontSize = 13.sp,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )

        // Círculo con la puntuación
        Surface(
            color = if (isWinner) Color(0xFF4CAF50) else Color(0xFF333333),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = score.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PlayerRow(name: String, score: Int, isWinner: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            color = if (isWinner) Color(0xFF4CAF50) else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
        Text(
            text = score.toString(),
            color = if (isWinner) Color(0xFF4CAF50) else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun Divider(color: Color, thickness: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.fillMaxWidth().height(thickness).background(color))
}

// --- DIBUJADO DE LAS LÍNEAS CONECTORAS ---
@Composable
fun BracketConnector(
    isTop: Boolean, // Si es el match de arriba o el de abajo en la pareja
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.width(40.dp).height(35.dp)) { // Mitad de la altura de la tarjeta
        val path = Path()
        val strokeWidth = 2.dp.toPx()
        val color = Color.Gray

        // Dibujamos lineas tipo "árbol"
        if (isTop) {
            // Línea que sale del centro derecha y baja
            path.moveTo(0f, size.height) // Empieza a la izquierda abajo (centro de la tarjeta visualmente)
            path.lineTo(size.width, size.height) // Va a la derecha
            path.lineTo(size.width, size.height * 2) // Baja hacia el centro de la siguiente ronda
        } else {
            // Línea que sale del centro derecha y sube
            path.moveTo(0f, 0f) // Izquierda arriba
            path.lineTo(size.width, 0f) // Derecha
            path.lineTo(size.width, -size.height) // Sube
        }

        // Nota: Dibujar curvas perfectas requiere Bezier, esto hace lineas rectas estilo bracket clásico
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }
}