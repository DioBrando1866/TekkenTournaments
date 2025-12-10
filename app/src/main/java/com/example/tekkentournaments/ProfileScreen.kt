package com.example.tekkentournaments

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// Imports de tus paquetes
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.repositories.UserRepository
import com.example.tekkentournaments.repositories.AuthRepository
import com.example.tekkentournaments.clases.EthereumService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    // --- ESTADOS DE NAVEGACIÓN Y DATOS ---
    var showTicketsScreen by remember { mutableStateOf(false) } // Controla si vemos el perfil o los tickets

    // Elevamos el estado de la wallet para que no se borre al cambiar de pantalla
    var walletAddress by remember { mutableStateOf("") }

    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Carga de datos inicial
    LaunchedEffect(Unit) {
        isLoading = true
        user = UserRepository.obtenerMiPerfil()
        isLoading = false
    }

    // --- LÓGICA DE NAVEGACIÓN ---
    if (showTicketsScreen) {
        // Muestra la pantalla de Tickets a pantalla completa
        MyTicketsScreen(
            walletAddress = walletAddress,
            onBack = { showTicketsScreen = false } // Al volver, regresamos al perfil
        )
    } else {
        // Muestra el Perfil Normal
        Scaffold(
            containerColor = Color(0xFF121212),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("FIGHTER PROFILE", fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { AuthRepository.logout(); onLogout() } }) {
                            Icon(Icons.Default.ExitToApp, "Salir", tint = Color(0xFFD32F2F))
                        }
                    }
                )
            }
        ) { innerPadding ->
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD32F2F))
                }
            } else if (user != null) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. TEKKEN CARD
                    TekkenCard(user = user!!)

                    // BOTÓN DEBUG
                    Button(
                        onClick = {
                            scope.launch {
                                UserRepository.debugSumarVictorias(user!!.id, user!!.wins)
                                user = UserRepository.obtenerMiPerfil() // Recarga rápida
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(40.dp)
                    ) {
                        Text("⚡ TEST: SUMAR +10 WINS", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. BIOGRAFÍA
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("BIOGRAFÍA", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(user!!.bio ?: "Sin biografía definida.", color = Color.LightGray, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. SECCIÓN CRYPTO (WEB3) - AHORA CON NAVEGACIÓN
                    CryptoSection(
                        currentWallet = walletAddress,
                        onWalletChange = { walletAddress = it },
                        onOpenTickets = { showTicketsScreen = true } // Al pulsar, cambiamos de pantalla
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. BOTÓN EDITAR
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EDITAR PERFIL", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }

    // DIÁLOGO DE EDICIÓN (Sin cambios)
    if (showEditDialog && user != null) {
        EditProfileDialog(
            currentUsername = user!!.username,
            currentBio = user!!.bio ?: "",
            currentStatus = user!!.status ?: "",
            currentImageUrl = user!!.profileImage,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newBio, newStatus, newImageUri ->
                scope.launch {
                    var finalImageUrl: String? = null
                    if (newImageUri != null) {
                        try {
                            val inputStream = context.contentResolver.openInputStream(newImageUri)
                            val bytes = inputStream?.readBytes()
                            inputStream?.close()
                            if (bytes != null) finalImageUrl = UserRepository.subirAvatar(user!!.id, bytes)
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                    UserRepository.actualizarPerfilCompleto(user!!.id, newName, newBio, newStatus, finalImageUrl)
                    user = UserRepository.obtenerMiPerfil()
                    showEditDialog = false
                }
            }
        )
    }
}

// ==========================================
// COMPONENTE CRYPTO (ACTUALIZADO CON BOTÓN TICKETS)
// ==========================================
@Composable
fun CryptoSection(
    currentWallet: String,
    onWalletChange: (String) -> Unit,
    onOpenTickets: () -> Unit
) {
    var balance by remember { mutableStateOf("---") }
    var blockchainWins by remember { mutableStateOf("---") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, Color(0xFF627EEA)), // Color Azul Ethereum
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyExchange, null, tint = Color(0xFF627EEA))
                Spacer(Modifier.width(8.dp))
                Text("TEKKEN WALLET (SEPOLIA)", color = Color(0xFF627EEA), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))
            Text("Conecta tu wallet para gestionar tus activos.", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))

            // Campo de Texto (Ahora controlado desde fuera)
            OutlinedTextField(
                value = currentWallet,
                onValueChange = onWalletChange,
                label = { Text("Dirección ETH (0x...)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF627EEA),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.Gray,
                    focusedLabelColor = Color(0xFF627EEA)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // FILA DE BOTONES DE ACCIÓN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón 1: Verificar Saldo (Azul)
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            balance = EthereumService.obtenerSaldoEth(currentWallet)
                            // Si quieres verificar victorias del contrato anterior también:
                            // blockchainWins = EthereumService.obtenerVictoriasBlockchain(currentWallet)
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF627EEA)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text("SALDO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Botón 2: Ver Tickets (Dorado)
                Button(
                    onClick = onOpenTickets,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)), // Oro
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ConfirmationNumber, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("TICKETS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Resultado Saldo
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Saldo Actual:", color = Color.White)
                Text(balance, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ... EditProfileDialog (igual que antes) ...
@Composable
fun EditProfileDialog(
    currentUsername: String,
    currentBio: String,
    currentStatus: String,
    currentImageUrl: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Uri?) -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }
    var bio by remember { mutableStateOf(currentBio) }
    var status by remember { mutableStateOf(currentStatus) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Editar Perfil", color = Color.White) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.Gray).clickable {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) AsyncImage(model = selectedImageUri, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else if (currentImageUrl != null) AsyncImage(model = currentImageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Nombre") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Lema") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            }
        },
        confirmButton = { Button(onClick = { onSave(username, bio, status, selectedImageUri) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("GUARDAR") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) } }
    )
}