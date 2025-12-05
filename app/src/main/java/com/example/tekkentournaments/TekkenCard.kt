package com.example.tekkentournaments

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tekkentournaments.clases.User

@Composable
fun TekkenCard(user: User) {
    // 1. LÓGICA DE RANGO: Calculamos el nivel según las victorias
    val (rankName, borderColors, glowColor) = when {
        user.wins >= 50 -> Triple("GOD OF DESTRUCTION", listOf(Color(0xFFFFD700), Color(0xFFFF8C00), Color(0xFFFFD700)), Color(0xFFFFD700)) // Dorado/Fuego
        user.wins >= 20 -> Triple("TEKKEN EMPEROR", listOf(Color(0xFFE040FB), Color(0xFF7C4DFF)), Color(0xFFE040FB)) // Morado
        user.wins >= 10 -> Triple("BATTLE RULER", listOf(Color(0xFFFF5252), Color(0xFFD32F2F)), Color(0xFFFF5252)) // Rojo
        user.wins >= 5 -> Triple("WARRIOR", listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)), Color(0xFF4CAF50)) // Verde
        else -> Triple("BEGINNER", listOf(Color.Gray, Color.DarkGray), Color.Gray) // Gris
    }

    // 2. ANIMACIÓN DEL BORDE (Efecto "Holo")
    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // 3. DISEÑO DE LA CARTA
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Fondo con degradado oscuro
                .background(Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color.Black)))
                // Borde animado (Solo si tiene rango alto, sino estático)
                .drawBehind {
                    if (user.wins >= 5) {
                        // Aquí podrías dibujar un borde complejo rotando con 'angle'
                        // Para simplificar, usamos un borde degradado estático pero llamativo
                    }
                }
                .border(
                    width = 4.dp,
                    brush = Brush.linearGradient(borderColors, tileMode = TileMode.Mirror),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // IMAGEN DE FONDO (Opcional: Marca de agua del personaje)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FOTO DE PERFIL (Izquierda)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(2.dp, glowColor, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.profileImage ?: "")
                            .error(android.R.drawable.ic_menu_camera)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // INFO DEL JUGADOR (Derecha)
                Column {
                    // Rango
                    Text(
                        text = rankName,
                        color = glowColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Nombre
                    Text(
                        text = user.username.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    // Victorias
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${user.wins}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                        Text(
                            text = " WINS",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Barra de Progreso al siguiente rango (Visual)
                    Spacer(modifier = Modifier.height(8.dp))
                    val nextRankWins = when {
                        user.wins < 5 -> 5
                        user.wins < 10 -> 10
                        user.wins < 20 -> 20
                        else -> 50
                    }
                    val progress = user.wins.toFloat() / nextRankWins.toFloat()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.DarkGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(glowColor)
                        )
                    }
                    Text(
                        text = "Next Rank: $nextRankWins wins",
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}