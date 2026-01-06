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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// --- TUS IMPORTS ---
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.repositories.UserRepository
import com.example.tekkentournaments.repositories.AuthRepository
import com.example.tekkentournaments.clases.EthereumService
import com.example.tekkentournaments.utils.LanguageUtils
import com.example.tekkentournaments.utils.ThemeUtils // <--- Para los colores
import com.example.tekkentournaments.utils.CharacterColors // Data class de colores
import com.example.tekkentournaments.CharacterGridSelector // <--- El grid nuevo
import com.example.tekkentournaments.ui.components.TekkenCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    // --- ESTADOS ---
    var showTicketsScreen by remember { mutableStateOf(false) }
    var walletAddress by remember { mutableStateOf("") }
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- CARGA DE DATOS ---
    LaunchedEffect(Unit) {
        isLoading = true
        user = UserRepository.obtenerMiPerfil()
        isLoading = false
    }

    // --- TEMA DINÁMICO ---
    // Calculamos los colores basándonos en el personaje del usuario
    val theme = remember(user?.characterMain) {
        ThemeUtils.getColorsForCharacter(user?.characterMain)
    }

    // --- NAVEGACIÓN INTERNA ---
    if (showTicketsScreen) {
        MyTicketsScreen(
            walletAddress = walletAddress,
            onBack = { showTicketsScreen = false }
        )
    } else {
        Scaffold(
            containerColor = theme.background, // 1. FONDO DINÁMICO
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.profile_title),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = theme.primary // 2. COLOR TÍTULO
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF1E1E1E).copy(alpha = 0.8f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = Color.White)
                        }
                    },
                    actions = {
                        // Botón Idioma
                        IconButton(onClick = {
                            val current = LanguageUtils.getCurrentLanguage(context)
                            val newLang = if (current == "es") "en" else "es"
                            LanguageUtils.setLocale(context, newLang)
                        }) {
                            Text(
                                text = LanguageUtils.getCurrentLanguage(context).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Botón Salir
                        IconButton(onClick = { scope.launch { AuthRepository.logout(); onLogout() } }) {
                            Icon(Icons.Default.ExitToApp, stringResource(R.string.btn_logout), tint = Color(0xFFD32F2F))
                        }
                    }
                )
            }
        ) { innerPadding ->
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = theme.primary)
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

                    // 1. TEKKEN CARD (Pasamos el usuario)
                    TekkenCard(user = user!!, theme = theme)
                    // 2. BIOGRAFÍA
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(1.dp, theme.secondary.copy(alpha = 0.3f)), // Borde sutil del color del tema
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.biography), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(user!!.bio ?: "---", color = Color.LightGray, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. SECCIÓN CRYPTO (WEB3)
                    // Le pasamos el tema para que pinte los iconos del color correcto
                    CryptoSection(
                        currentWallet = walletAddress,
                        theme = theme, // <--- Pasamos el tema
                        onWalletChange = { walletAddress = it },
                        onOpenTickets = { showTicketsScreen = true }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. BOTÓN EDITAR (Color del tema)
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary), // <--- Botón dinámico
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.edit_profile), color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }

    // --- DIÁLOGO EDITAR PERFIL (Actualizado) ---
    if (showEditDialog && user != null) {
        EditProfileDialog(
            currentUsername = user!!.username,
            currentBio = user!!.bio ?: "",
            currentStatus = user!!.status ?: "",
            currentImageUrl = user!!.profileImage,
            currentCharMain = user!!.characterMain, // Pasamos el Main actual
            onDismiss = { showEditDialog = false },
            onSave = { newName, newBio, newStatus, newChar, newImageUri ->
                scope.launch {
                    // 1. IMPORTANTE: Empezamos con la URL actual para no perderla si no se sube nada nuevo
                    var finalImageUrl: String? = user!!.profileImage

                    // 2. Si el usuario seleccionó una foto nueva, intentamos subirla
                    if (newImageUri != null) {
                        try {
                            val inputStream = context.contentResolver.openInputStream(newImageUri)
                            val bytes = inputStream?.readBytes()
                            inputStream?.close()

                            if (bytes != null) {
                                val uploadedUrl = UserRepository.subirAvatar(user!!.id, bytes)
                                // Solo si la subida retorna una URL válida, actualizamos la variable
                                if (uploadedUrl != null) {
                                    finalImageUrl = uploadedUrl
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Aquí podrías mostrar un Toast: "Error al subir imagen"
                        }
                    }

                    // 3. Actualización en BD
                    UserRepository.actualizarPerfilCompleto(
                        userId = user!!.id,
                        nuevoUsername = newName,
                        nuevoBio = newBio,
                        nuevoStatus = newStatus,
                        nuevoCharacter = newChar,
                        // Aquí pasamos la nueva URL o la vieja, pero evitamos pasar null accidentalmente
                        nuevaImagenUrl = finalImageUrl
                    )

                    // 4. Recarga con "Truco" para romper la Caché (Cache Busting)
                    val refreshedUser = UserRepository.obtenerMiPerfil()

                    if (refreshedUser != null) {
                        // Si la URL es la misma (el servidor sobreescribió el archivo), Coil no se entera.
                        // Añadimos un timestamp (?t=12345) al final para forzar a Coil a descargarla de nuevo.
                        val currentUrl = refreshedUser.profileImage
                        if (currentUrl != null) {
                            val separator = if (currentUrl.contains("?")) "&" else "?"
                            val urlNoCache = "$currentUrl${separator}t=${System.currentTimeMillis()}"

                            // Asumimos que User es una data class. Usamos copy para actualizar la URL en local
                            user = refreshedUser.copy(profileImage = urlNoCache)
                        } else {
                            user = refreshedUser
                        }
                    }

                    showEditDialog = false
                }
            }
        )
    }
}

// ==========================================
// COMPONENTE CRYPTO (ADAPTADO AL TEMA)
// ==========================================
@Composable
fun CryptoSection(
    currentWallet: String,
    theme: CharacterColors, // Recibe los colores
    onWalletChange: (String) -> Unit,
    onOpenTickets: () -> Unit
) {
    var balance by remember { mutableStateOf("---") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, theme.primary), // Borde del color del personaje
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyExchange, null, tint = theme.primary)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.wallet_title), color = theme.primary, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.wallet_subtitle), color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = currentWallet,
                onValueChange = onWalletChange,
                label = { Text(stringResource(R.string.wallet_hint)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.Gray,
                    focusedLabelColor = theme.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            balance = EthereumService.obtenerSaldoEth(currentWallet)
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                    else Text(stringResource(R.string.btn_check_balance), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenTickets,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)), // Tickets siempre dorado
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ConfirmationNumber, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_tickets), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.balance_label), color = Color.White)
                Text(balance, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// DIÁLOGO EDICIÓN (CON GRID SELECTOR)
// ==========================================
@Composable
fun EditProfileDialog(
    currentUsername: String,
    currentBio: String,
    currentStatus: String,
    currentImageUrl: String?,
    currentCharMain: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Uri?) -> Unit // Devuelve también el char
) {
    var username by remember { mutableStateOf(currentUsername) }
    var bio by remember { mutableStateOf(currentBio) }
    var status by remember { mutableStateOf(currentStatus) }
    var selectedChar by remember { mutableStateOf(currentCharMain ?: "Random") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text(stringResource(R.string.edit_profile), color = Color.White) },
        text = {
            // Scroll necesario porque el Grid ocupa espacio
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // 1. FOTO
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

                // 2. CAMPOS
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Nombre") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Lema") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))

                Spacer(Modifier.height(16.dp))

                // 3. SELECTOR DE PERSONAJE VISUAL (GRID)
                CharacterGridSelector(
                    gameVersion = "Tekken 8",
                    selectedCharacter = selectedChar,
                    onCharacterSelected = { selectedChar = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(username, bio, status, selectedChar, selectedImageUri) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) { Text(stringResource(R.string.btn_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.exit), color = Color.Gray) }
        }
    )
}