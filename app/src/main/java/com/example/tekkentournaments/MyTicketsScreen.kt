package com.example.tekkentournaments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.tekkentournaments.clases.EthereumService

@Composable
fun MyTicketsScreen(
    walletAddress: String, // Recibimos la wallet del usuario
    onBack: () -> Unit
) {
    var hasTicket by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Verificamos en Blockchain al entrar
    LaunchedEffect(Unit) {
        isLoading = true
        if (walletAddress.isNotEmpty()) {
            hasTicket = EthereumService.tieneEntradaNFT(walletAddress)
        }
        isLoading = false
    }

    // Diseño de Fondo Oscuro
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("MY NFT WALLET", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF00E5FF))
            Spacer(Modifier.height(16.dp))
            Text("Verificando en Blockchain...", color = Color.Gray)
        } else {
            if (hasTicket) {
                // --- CASO 1: TIENE ENTRADA (MUESTRA EL NFT) ---
                TicketDorado(walletAddress)

                Spacer(Modifier.height(30.dp))

                Text("✅ Entrada verificada en Sepolia Network", color = Color.Green, fontSize = 12.sp)

                Spacer(Modifier.height(20.dp))

                // Botón Simulado de Reventa
                Button(
                    onClick = { /* Lógica futura */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Sell, null, tint = Color.Yellow)
                    Spacer(Modifier.width(8.dp))
                    Text("VENDER PLAZA (MARKETPLACE)", color = Color.White)
                }

            } else {
                // --- CASO 2: NO TIENE ENTRADA ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SentimentDissatisfied, null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No tienes entradas activas", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Compra un NFT para participar.", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        // Aquí recargamos por si acaba de comprarla en Remix
                        scope.launch {
                            isLoading = true
                            hasTicket = EthereumService.tieneEntradaNFT(walletAddress)
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("ACTUALIZAR WALLET", color = Color.Black)
                }
            }
        }
    }
}

// --- COMPONENTE VISUAL: TICKET DORADO ---
@Composable
fun TicketDorado(owner: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, Color(0xFFFFD700)), // Borde Dorado
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFB8860B), Color(0xFFFFD700), Color(0xFFB8860B)) // Degradado Oro
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("IRON FIST 2025", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.Black)
                    Icon(Icons.Default.QrCode2, null, tint = Color.Black, modifier = Modifier.size(40.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Text(" OFFICIAL NFT TICKET", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Column {
                    Text("HOLDER:", fontSize = 10.sp, color = Color.Black.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    Text(
                        text = owner.take(10) + "..." + owner.takeLast(4),
                        fontSize = 14.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}