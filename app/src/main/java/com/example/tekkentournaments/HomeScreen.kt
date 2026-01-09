package com.example.tekkentournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // <--- IMPORTANTE
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.repositories.TournamentRepository
import com.example.tekkentournaments.repositories.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToList: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFrameData: () -> Unit = {}
) {
    var featuredTournament by remember { mutableStateOf<Tournament?>(null) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val tournaments = TournamentRepository.obtenerTorneos()
            featuredTournament = tournaments.firstOrNull()
            currentUser = UserRepository.obtenerMiPerfil()
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.home_title), fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color(0xFFD32F2F)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // --- 1. CABECERA: PLAYER ID CARD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1E1E1E), Color(0xFF2C2C2C))
                        )
                    )
            ) {
                Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(Color(0xFFD32F2F)))

                if (currentUser != null) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentUser?.profileImage ?: "")
                                .crossfade(true)
                                .error(android.R.drawable.ic_menu_camera)
                                .build(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.Gray)
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.ready_to_fight),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = currentUser?.username?.uppercase() ?: stringResource(R.string.fighter_default),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                            if (!currentUser?.status.isNullOrBlank()) {
                                Text(
                                    text = "\"${currentUser?.status}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFD32F2F),
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else if (isLoading) {
                    TekkenLoader()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. EVENTO DESTACADO ---
            PaddingBox(title = stringResource(R.string.next_tournament_title)) {
                if (featuredTournament != null) {
                    FeaturedTournamentCard(featuredTournament!!)
                } else if (!isLoading) {
                    Text(
                        stringResource(R.string.no_tournaments_msg),
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. ACCIONES RÁPIDAS ---
            PaddingBox(title = stringResource(R.string.quick_actions)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MenuButton(
                        text = stringResource(R.string.explore_tournaments),
                        icon = Icons.Default.TrendingUp,
                        onClick = onNavigateToList
                    )

                    // --- NUEVO BOTÓN DE FRAME DATA ---
                    MenuButton(
                        text = "Frame Data",
                        icon = Icons.Default.QueryStats,
                        onClick = onNavigateToFrameData
                    )

                    MenuButton(
                        text = stringResource(R.string.my_player_profile),
                        icon = Icons.Default.SportsEsports,
                        onClick = onNavigateToProfile
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun PaddingBox(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun FeaturedTournamentCard(tournament: Tournament) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(colors = listOf(Color(0xFF8B0000), Color(0xFF2C2C2C)))
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color.White, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            stringResource(R.string.featured_label),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = tournament.date ?: stringResource(R.string.date_tbd),
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = tournament.name.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${tournament.tournamentType} • ${tournament.maxPlayers} ${stringResource(R.string.slots_label)}",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MenuButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFD32F2F))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}