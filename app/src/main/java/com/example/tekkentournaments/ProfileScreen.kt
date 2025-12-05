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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

// Imports de tus paquetes
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.repositories.UserRepository
import com.example.tekkentournaments.repositories.AuthRepository
//import com.example.tekkentournaments.TekkenCard// Asegúrate de tener el archivo TekkenCard.kt creado

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

    // Función para recargar datos
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // --- 1. TEKKEN CARD (Componente Visual) ---
                TekkenCard(user = user!!)

                Spacer(modifier = Modifier.height(24.dp))

                // --- 2. BIOGRAFÍA ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("BIOGRAFÍA", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user!!.bio ?: "Sin biografía definida.",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- 3. BOTÓN EDITAR ---
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

    // --- DIÁLOGO DE EDICIÓN ---
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

                    // Si hay imagen nueva, subirla a Supabase Storage
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

                    // Actualizar en base de datos
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

                // Selector de Imagen
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
                Text("Cambiar foto", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Nombre de Usuario") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = status, onValueChange = { status = it },
                    label = { Text("Lema / Estado") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bio, onValueChange = { bio = it },
                    label = { Text("Biografía") }, maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(username, bio, status, selectedImageUri) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                Text("GUARDAR CAMBIOS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}