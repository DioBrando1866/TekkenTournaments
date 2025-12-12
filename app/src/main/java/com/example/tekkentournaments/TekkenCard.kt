package com.example.tekkentournaments

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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

// Asegúrate de tener estos imports
import androidx.compose.material.icons.rounded.SportsMma // Icono de guante/lucha
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import com.example.tekkentournaments.utils.CharacterColors // Tu clase de colores

@Composable
fun TekkenCard(
    user: User,
    theme: CharacterColors // <--- NUEVO: Recibimos el tema para colorear la etiqueta
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // Un poco más alto para que quepa todo
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // IMAGEN DE FONDO (Opcional: podrías poner la foto del personaje aquí con alpha bajo)
            if (user.profileImage != null) {
                AsyncImage(
                    model = user.profileImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.3f) // Muy transparente
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
            }

            // DEGRADADO SUPERIOR PARA QUE SE LEA EL TEXTO
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 0f,
                            endY = 400f
                        )
                    )
            )

            // CONTENIDO
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 1. AVATAR CIRCULAR
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(2.dp, theme.primary, CircleShape) // Borde del color del Main
                        .padding(3.dp)
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

                // 2. TEXTOS Y ETIQUETA
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text(
                        text = user.username.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )

                    // --- NUEVA ETIQUETA DE MAIN ---
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = theme.primary.copy(alpha = 0.15f), // Fondo semitransparente del color del tema
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icono pequeño de lucha
                            Icon(
                                imageVector = Icons.Rounded.SportsMma,
                                contentDescription = null,
                                tint = theme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))

                            // Texto: MAIN: KAZUYA
                            Text(
                                text = "${stringResource(R.string.main_character_label)} ${user.characterMain?.uppercase() ?: "RANDOM"}",
                                color = theme.primary, // Texto del color del tema
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    // -----------------------------

                    Spacer(Modifier.height(8.dp))

                    // Victorias
                    Text(
                        text = "${stringResource(R.string.wins_label)}: ${user.wins}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}