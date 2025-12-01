package com.example.tekkentournaments

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

// --- TUS NUEVOS IMPORTS ---
// Asegúrate de que estos coincidan exactamente con tus paquetes
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.repositories.UserRepository
import com.example.tekkentournaments.repositories.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit // Este es el callback que activará el botón
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Cargar datos
    fun cargarDatos() {
        scope.launch {
            isLoading = true
            user = UserRepository.obtenerMiPerfil()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FIGHTER PROFILE", fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White
                ),
                // --- AQUÍ ESTÁ EL BOTÓN DE ATRÁS ---
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, // Icono de flecha
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                // -----------------------------------
                actions = {
                    IconButton(onClick = { scope.launch { AuthRepository.logout(); onLogout() } }) {
                        Icon(Icons.Default.ExitToApp, "Salir", tint = Color(0xFFD32F2F))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFD32F2F)) }
        } else if (user != null) {
            Column(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 1. AVATAR CON COIL
                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(134.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFFD32F2F), Color.Black))))

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user!!.profileImage ?: "")
                            .crossfade(true)
                            .error(android.R.drawable.ic_menu_camera)
                            .build(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2C))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. DATOS
                Text(user!!.username.uppercase(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)

                if (!user!!.status.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("\"${user!!.status}\"", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Text(user!!.bio ?: "Sin biografía.", color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp).fillMaxWidth())
                }

                Spacer(modifier = Modifier.weight(1f))

                // BOTÓN EDITAR
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
            }
        }
    }

    // DIÁLOGO DE EDICIÓN
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

                            if (bytes != null) {
                                finalImageUrl = UserRepository.subirAvatar(user!!.id, bytes)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    UserRepository.actualizarPerfilCompleto(
                        userId = user!!.id,
                        nuevoUsername = newName,
                        nuevoBio = newBio,
                        nuevoStatus = newStatus,
                        nuevaImagenUrl = finalImageUrl
                    )
                    cargarDatos()
                    showEditDialog = false
                }
            }
        )
    }
}

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

                // SELECTOR DE IMAGEN
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else if (currentImageUrl != null) {
                        AsyncImage(model = currentImageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                    }

                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.8f))
                    }
                }
                Text("Toca para cambiar foto", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))

                Spacer(modifier = Modifier.height(16.dp))

                // CAMPOS
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Nombre de Usuario") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = status, onValueChange = { status = it },
                    label = { Text("Lema / Estado") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bio, onValueChange = { bio = it },
                    label = { Text("Biografía") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(username, bio, status, selectedImageUri) }) {
                Text("GUARDAR CAMBIOS", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}