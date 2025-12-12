package com.example.tekkentournaments

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.rounded.SportsMma
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource // <--- IMPORTANTE
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.tekkentournaments.clases.EthereumService

@Composable
fun MyTicketsScreen(
    walletAddress: String,
    onBack: () -> Unit
) {
    var hasTicket by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        if (walletAddress.isNotEmpty()) {
            kotlinx.coroutines.delay(500)
            hasTicket = EthereumService.tieneEntradaNFT(walletAddress)
        }
        isLoading = false
    }

    // --- FONDO TECNOLÓGICO ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // Capa 1: Degradado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E1E2E), Color(0xFF0A0A0A)),
                        center = Offset.Unspecified,
                        radius = 1000f
                    )
                )
        )
        // Capa 2: Grid
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            val step = 40.dp.toPx()
            for (i in 0 until (size.width / step).toInt()) {
                drawLine(Color.White, start = Offset(i * step, 0f), end = Offset(i * step, size.height), strokeWidth = 1f)
            }
            for (i in 0 until (size.height / step).toInt()) {
                drawLine(Color.White, start = Offset(0f, i * step), end = Offset(size.width, i * step), strokeWidth = 1f)
            }
        }

        // --- CONTENIDO PRINCIPAL ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.tickets_title), // "NFT ASSETS & TICKETS"
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (isLoading) {
                Box(Modifier.height(300.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFFFD700), strokeWidth = 4.dp, modifier = Modifier.size(50.dp))
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.sync_blockchain), color = Color.Gray, fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.network_sepolia), color = Color.DarkGray, fontSize = 12.sp)
                    }
                }
            } else {
                if (hasTicket) {
                    // --- CASO 1: TIENE ENTRADA (Premium) ---
                    TicketDoradoPremium(walletAddress)

                    Spacer(Modifier.height(40.dp))

                    // Indicador de estado
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFF112211), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Verified, null, tint = Color(0xFF00FF00), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.verified_on_chain),
                            color = Color(0xFF00FF00),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Botones de Acción
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionButton(Icons.Default.Share, stringResource(R.string.btn_share), Color(0xFF333333), Modifier.weight(1f)) {}
                        ActionButton(Icons.Default.Sell, stringResource(R.string.btn_sell_soon), Color(0xFF553311), Modifier.weight(1f)) {}
                    }
                    Spacer(Modifier.height(20.dp))

                } else {
                    // --- CASO 2: NO TIENE ENTRADA (Slot Vacío) ---
                    EmptyTicketSlot()

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                hasTicket = EthereumService.tieneEntradaNFT(walletAddress)
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.btn_update_wallet), // "ACTUALIZAR WALLET"
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES VISUALES PREMIUM
// ==========================================

@Composable
fun TicketDoradoPremium(owner: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val shadowScale by infiniteTransition.animateFloat(
        initialValue = 8.dp.value, targetValue = 20.dp.value,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "shadow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .shadow(elevation = shadowScale.dp, spotColor = Color(0xFFFFD700), shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(3.dp, Brush.linearGradient(listOf(Color(0xFFFFE082), Color(0xFFB8860B)))),
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Fondo Metálico
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF8C6100),
                                Color(0xFFDBA514),
                                Color(0xFFFFE082),
                                Color(0xFFDBA514),
                                Color(0xFF8C6100)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
            )

            // 2. Patrón
            Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
                drawCircle(Color.Black, center = center, radius = size.minDimension / 3, style = Stroke(width = 20f))
                drawLine(Color.Black, start = Offset(0f, 0f), end = Offset(size.width, size.height), strokeWidth = 5f)
            }

            // 3. Contenido
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Cabecera Ticket
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = stringResource(R.string.ticket_hub_name), // "TEKKEN HUB"
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.7f),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = stringResource(R.string.event_iron_fist), // "IRON FIST 2025"
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            color = Color(0xFF3E2723),
                            letterSpacing = 1.sp
                        )
                    }
                    // Sello VIP
                    Box(
                        modifier = Modifier
                            .border(2.dp, Color.Black, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.vip_access),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }

                // Icono Central
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.SportsMma,
                        contentDescription = null,
                        tint = Color(0xFF3E2723).copy(alpha = 0.8f),
                        modifier = Modifier.size(80.dp)
                    )
                }

                // Pie de página
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = stringResource(R.string.asset_holder),
                            fontSize = 10.sp,
                            color = Color.Black.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = owner.take(8) + "...." + owner.takeLast(6),
                            fontSize = 16.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Icon(Icons.Default.QrCode, null, tint = Color.Black, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyTicketSlot() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.2f)),
                    tileMode = TileMode.Repeated
                ),
                shape = RoundedCornerShape(24.dp),
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1A1A1A).copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(30.dp)) {
            Icon(
                imageVector = Icons.Outlined.ConfirmationNumber,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(60.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.empty_slot), // "SLOT VACÍO"
                color = Color.Gray,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.empty_slot_desc), // Descripción larga
                color = Color.DarkGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Icon(Icons.Default.Lock, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}