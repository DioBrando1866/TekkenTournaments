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

// IMPORTANTE: Asegúrate de que estos imports coinciden con tus paquetes
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.repositories.UserRepository
import com.example.tekkentournaments.repositories.AuthRepository
import com.example.tekkentournaments.utils.ThemeUtils
import com.example.tekkentournaments.CharacterGridSelector
import com.example.tekkentournaments.ui.components.TekkenCard

// Si tienes TekkenCard en otro paquete (ej: ui.components), impórtalo aquí.
// Si está en el mismo paquete (com.example.tekkentournaments), no hace falta import.
// import com.example.tekkentournaments.ui.components.TekkenCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        isLoading = true
        user = UserRepository.obtenerMiPerfil()
        isLoading = false
    }

    // Calcular Tema Dinámico basado en el Main
    val theme = remember(user?.characterMain) { ThemeUtils.getColorsForCharacter(user?.characterMain) }

    Scaffold(
        containerColor = theme.background, // Fondo dinámico
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_title), fontWeight = FontWeight.Bold, color = theme.primary) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1E1E1E).copy(alpha = 0.8f)),
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                actions = {
                    IconButton(onClick = { scope.launch { AuthRepository.logout(); onLogout() } }) { Icon(Icons.Default.ExitToApp, "Logout", tint = Color(0xFFD32F2F)) }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = theme.primary) }
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

                // 1. TARJETA TEKKEN (Con Banner y Rangos)
                TekkenCard(user = user!!, theme = theme)

                Spacer(modifier = Modifier.height(24.dp))

                // 2. BIOGRAFÍA Y ESTADO
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    border = BorderStroke(1.dp, theme.secondary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Título Bio
                        Text(stringResource(R.string.biography), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        // Contenido Bio
                        Text(user!!.bio ?: "---", color = Color.LightGray, fontSize = 14.sp)

                        // --- ESTADO (CORREGIDO PARA EVITAR ERROR DE SMART CAST) ---
                        user?.status?.let { safeStatus ->
                            if (safeStatus.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Estado", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(safeStatus, color = theme.primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        // ---------------------------------------------------------
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. BOTÓN EDITAR
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.edit_profile), color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. BOTÓN DEBUG (Solo pruebas)
                TextButton(
                    onClick = {
                        scope.launch {
                            UserRepository.debugSumarVictorias(user!!.id, user!!.wins)
                            user = UserRepository.obtenerMiPerfil()
                        }
                    }
                ) {
                    Text("+10 Wins (Test Rank)", color = Color.Gray, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }

    // --- DIÁLOGO DE EDICIÓN ---
    if (showEditDialog && user != null) {
        EditProfileDialog(
            currentUser = user!!,
            onDismiss = { showEditDialog = false },
            // Recibe todos los datos para guardar
            onSave = { newName, newBio, newStatus, newChar, newAvatarUri, newBannerUri ->
                scope.launch {
                    isLoading = true // Indicamos carga visual

                    // 1. Subir Avatar si hay uno nuevo
                    var finalAvatarUrl: String? = null
                    if (newAvatarUri != null) {
                        context.contentResolver.openInputStream(newAvatarUri)?.use { stream ->
                            finalAvatarUrl = UserRepository.subirAvatar(user!!.id, stream.readBytes())
                        }
                    }

                    // 2. Subir Banner si hay uno nuevo
                    var finalBannerUrl: String? = null
                    if (newBannerUri != null) {
                        context.contentResolver.openInputStream(newBannerUri)?.use { stream ->
                            finalBannerUrl = UserRepository.subirBanner(user!!.id, stream.readBytes())
                        }
                    }

                    // 3. Guardar en BD
                    UserRepository.actualizarPerfilCompleto(
                        userId = user!!.id,
                        nuevoUsername = newName,
                        nuevoBio = newBio,
                        nuevoStatus = newStatus,
                        nuevoCharacter = newChar,
                        nuevaImagenUrl = finalAvatarUrl,
                        nuevoBannerUrl = finalBannerUrl
                    )

                    // 4. Refrescar
                    user = UserRepository.obtenerMiPerfil()
                    isLoading = false
                    showEditDialog = false
                }
            }
        )
    }
}

// ==========================================
// COMPONENTE: DIÁLOGO DE EDICIÓN
// ==========================================
@Composable
fun EditProfileDialog(
    currentUser: User,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Uri?, Uri?) -> Unit
) {
    var username by remember { mutableStateOf(currentUser.username) }
    var bio by remember { mutableStateOf(currentUser.bio ?: "") }
    var status by remember { mutableStateOf(currentUser.status ?: "") }
    var selectedChar by remember { mutableStateOf(currentUser.characterMain ?: "Random") }

    // Variables para las imágenes temporales
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBannerUri by remember { mutableStateOf<Uri?>(null) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> selectedAvatarUri = uri }
    val bannerPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> selectedBannerUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text(stringResource(R.string.edit_profile), color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- 1. SELECTOR DE BANNER ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF333333))
                        .clickable { bannerPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                ) {
                    if (selectedBannerUri != null) {
                        AsyncImage(model = selectedBannerUri, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else if (currentUser.bannerImage != null) {
                        AsyncImage(model = currentUser.bannerImage, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, null, tint = Color.Gray)
                            Text("Toca para añadir Banner", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // --- 2. SELECTOR DE AVATAR ---
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable { avatarPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedAvatarUri != null) AsyncImage(model = selectedAvatarUri, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else if (currentUser.profileImage != null) AsyncImage(model = currentUser.profileImage, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                }
                Text("Foto de Perfil", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(16.dp))

                // --- 3. CAMPOS DE TEXTO ---
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Nombre de Usuario") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedBorderColor = Color.White, unfocusedBorderColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = status, onValueChange = { status = it },
                    label = { Text("Estado / Lema") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedBorderColor = Color.White, unfocusedBorderColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = bio, onValueChange = { bio = it },
                    label = { Text("Biografía") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedBorderColor = Color.White, unfocusedBorderColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(Modifier.height(16.dp))

                // --- 4. SELECTOR DE MAIN ---
                Text("Selecciona tu Main:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                CharacterGridSelector(
                    gameVersion = "Tekken 8",
                    selectedCharacter = selectedChar,
                    onCharacterSelected = { selectedChar = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(username, bio, status, selectedChar, selectedAvatarUri, selectedBannerUri) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(8.dp)
            ) { Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.exit), color = Color.Gray) }
        }
    )
}