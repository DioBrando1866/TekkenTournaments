package com.example.tekkentournaments

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SportsMma
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TekkenLoader(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD32F2F) // Rojo Tekken por defecto
) {
    // --- ANIMACIONES ---
    val infiniteTransition = rememberInfiniteTransition(label = "loader_transitions")

    // 1. Rotación del anillo (Rápida y agresiva)
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // 2. Latido del Puño (Pulse)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // --- MENSAJES ALEATORIOS ---
    val messages = listOf(
        "CONNECTING TO ZAIBATSU...",
        "SYNCING FIGHT DATA...",
        "PREPARING ARENA...",
        "GET READY FOR THE NEXT BATTLE",
        "ANALYZING MATCHUPS..."
    )
    var currentMessageIndex by remember { mutableStateOf(0) }

    // Cambiar mensaje cada 1.5 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            currentMessageIndex = (currentMessageIndex + 1) % messages.size
        }
    }

    // --- UI ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A).copy(alpha = 0.95f)), // Fondo casi negro
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Box(contentAlignment = Alignment.Center) {
                // CAPA 1: Anillo de Energía
                Canvas(modifier = Modifier.size(100.dp)) {
                    rotate(angle) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color.Transparent, color.copy(alpha = 0.5f), color)
                            ),
                            startAngle = 0f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // CAPA 2: Puño Central Palpitante
                Icon(
                    imageVector = Icons.Rounded.SportsMma,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .size(40.dp)
                        .scale(scale)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // CAPA 3: Texto Parpadeante
            Text(
                text = messages[currentMessageIndex],
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.labelLarge
            )

            // Barra de progreso decorativa
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                color = color,
                trackColor = Color(0xFF333333),
                modifier = Modifier.width(150.dp).height(2.dp)
            )
        }
    }
}