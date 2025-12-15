package com.example.tekkentournaments.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.SportsMma
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tekkentournaments.R
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.utils.CharacterColors
import com.example.tekkentournaments.utils.TekkenRankUtils

@Composable
fun TekkenCard(
    user: User,
    theme: CharacterColors // El tema del personaje "Main" para la etiqueta pequeña
) {
    // --- CÁLCULOS DE RANGO ---
    // Usamos 'remember' para no recalcular si no cambian las victorias
    val rank = remember(user.wins) { TekkenRankUtils.getRankFromWins(user.wins) }
    val nextRank = remember(user.wins) { TekkenRankUtils.getNextRank(user.wins) }
    val progress = remember(user.wins) { TekkenRankUtils.getProgressToNextRank(user.wins) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp), // Altura suficiente para banner y datos
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFF333333)) // Borde sutil oscuro
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. FONDO BASE NEGRO
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)))

            // 2. IMAGEN DE BANNER PERSONALIZADO
            if (user.bannerImage != null) {
                AsyncImage(
                    model = user.bannerImage,
                    contentDescription = "Banner",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.6f) // Transparencia para que no compita con el texto
                )
            }

            // 3. DEGRADADO OSCURO (Esencial para leer el texto)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f), // Arriba un poco oscuro
                                Color.Black.copy(alpha = 0.95f) // Abajo casi negro sólido
                            ),
                            startY = 0f,
                            endY = 700f
                        )
                    )
            )

            // 4. CONTENIDO DE LA TARJETA
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // --- AVATAR DE PERFIL ---
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        // El borde del avatar usa el color del RANGO actual
                        .border(3.dp, rank.color, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    if (user.profileImage != null) {
                        AsyncImage(model = user.profileImage, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.align(Alignment.Center))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // --- COLUMNA DE INFORMACIÓN ---
                Column(modifier = Modifier.weight(1f).padding(bottom = 4.dp)) {
                    // Nombre de usuario
                    Text(
                        text = user.username.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    // ETIQUETA DEL MAIN (Usa el color del TEMA del personaje)
                    Surface(
                        color = theme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.SportsMma, null, tint = theme.primary, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "MAIN: ${user.characterMain?.uppercase() ?: "RANDOM"}", // Asegúrate de tener el string resource o usa texto directo
                                color = theme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // --- SECCIÓN DE RANGO ---
                    Text("RANK:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    // Título del Rango (Color del Rango)
                    Text(
                        text = rank.title,
                        color = rank.color,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    // Barra de Progreso al siguiente rango
                    if (nextRank != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = progress,
                                color = rank.color,
                                trackColor = Color.DarkGray,
                                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${user.wins}/${nextRank.minWins}", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Next: ${nextRank.title}", color = Color.DarkGray, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                    } else {
                        // Rango máximo alcanzado
                        Text("MAX RANK ACHIEVED", color = Color(0xFFFF00CC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}