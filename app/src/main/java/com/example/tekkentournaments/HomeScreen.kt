package com.example.tekkentournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.repositories.TournamentRepository
import io.github.jan.supabase.postgrest.from
import supabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToList: () -> Unit = {}, // Callback para ir a la lista completa
    onNavigateToProfile: () -> Unit = {}
) {
    // Estado para datos informativos
    var featuredTournament by remember { mutableStateOf<Tournament?>(null) }
    var totalPlayers by remember { mutableStateOf(0) }
    var totalTournaments by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar datos "resumen" (Solo lo necesario para informar)
    LaunchedEffect(Unit) {
        try {
            // 1. Traer el torneo más reciente para destacarlo
            val tournaments = TournamentRepository.obtenerTorneos()
            featuredTournament = tournaments.firstOrNull()
            totalTournaments = tournaments.size

            // 2. Contar jugadores (Count simple)
            val playersCount = supabase.from("users").select().countOrNull()
            totalPlayers = playersCount?.toInt() ?: 0

            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color(0xFF121212), // Fondo oscuro estilo Tekken
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TEKKEN HUB", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color(0xFFD32F2F) // Rojo Tekken
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Scroll por si la pantalla es pequeña
        ) {

            // --- 1. HERO SECTION (BIENVENIDA) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Bienvenido, Luchador.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "La batalla por el Puño de Hierro continúa.\nRevisa los eventos destacados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }
            }

            // --- 2. ESTADÍSTICAS RÁPIDAS (Estilo Dashboard) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Jugadores",
                    value = if (isLoading) "-" else totalPlayers.toString(),
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Torneos",
                    value = if (isLoading) "-" else totalTournaments.toString(),
                    icon = Icons.Default.EmojiEvents,
                    modifier = Modifier.weight(1f)
                )
            }

            // --- 3. SECCIÓN INFORMATIVA: EVENTO DESTACADO ---
            Text(
                text = "Evento Principal",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD32F2F))
                }
            } else {
                featuredTournament?.let { torneo ->
                    FeaturedTournamentCard(torneo)
                } ?: Text(
                    "No hay eventos programados.",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. MENÚ DE ACCESO (Botones grandes) ---
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                MenuButton(
                    text = "Explorar Todos los Torneos",
                    icon = Icons.Default.TrendingUp,
                    onClick = onNavigateToList
                )

                MenuButton(
                    text = "Mi Perfil de Jugador",
                    icon = Icons.Default.SportsEsports,
                    onClick = onNavigateToProfile
                )
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFD32F2F))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}

@Composable
fun FeaturedTournamentCard(tournament: Tournament) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.15f)), // Fondo rojo sutil
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge(containerColor = Color(0xFFD32F2F)) {
                    Text("PRÓXIMAMENTE", color = Color.White, modifier = Modifier.padding(4.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tournament.date ?: "Fecha TBD",
                    color = Color(0xFFFFCDD2), // Rojo muy claro
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tournament.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Formato: ${tournament.tournamentType} • ${tournament.maxPlayers} Jugadores",
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun MenuButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}