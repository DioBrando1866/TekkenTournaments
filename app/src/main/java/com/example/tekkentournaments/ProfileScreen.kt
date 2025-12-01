package com.example.tekkentournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tekkentournaments.clases.User
import com.example.tekkentournaments.repositories.AuthRepository
import com.example.tekkentournaments.repositories.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Estados para el diálogo de edición
    var showEditDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Función para recargar los datos (útil tras editar)
    fun cargarDatos() {
        scope.launch {
            isLoading = true
            user = UserRepository.obtenerMiPerfil()
            isLoading = false
        }
    }

    // Cargar datos al entrar
    LaunchedEffect(Unit) {
        cargarDatos()
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FIGHTER PROFILE", fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            AuthRepository.logout()
                            onLogout()
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir", tint = Color(0xFFD32F2F))
                    }
                }
            )
        }
    ) { innerPadding ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFD32F2F))
            }
        } else if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error al cargar perfil.", color = Color.Gray)
            }
        } else {
            // UI DEL PERFIL
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 1. AVATAR
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFD32F2F), Color.Black)))
                    )
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = Color(0xFF2C2C2C)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(20.dp),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. NOMBRE DE USUARIO
                Text(
                    text = user!!.username.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                // 3. STATUS (LEMA) - CORREGIDO
                // Ahora se muestra como un texto destacado/lema debajo del nombre
                if (!user!!.status.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"${user!!.status}\"", // Entre comillas para dar efecto de cita
                        color = Color(0xFFD32F2F), // Rojo Tekken
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp
                    )
                }

                // 4. BIOGRAFÍA
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = user!!.bio ?: "Sin biografía definida.",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 5. ESTADÍSTICAS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat(label = "RANK", value = "1st Dan", icon = Icons.Default.Shield)
                    ProfileStat(label = "WINS", value = "0", icon = Icons.Default.Star)
                    ProfileStat(label = "MATCHES", value = "0", icon = Icons.Default.Edit)
                }

                Spacer(modifier = Modifier.weight(1f)) // Empuja el botón al fondo

                // 6. BOTÓN EDITAR (Abre el diálogo)
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EDITAR PERFIL", color = Color.White)
                }
            }
        }
    }

    // --- DIÁLOGO DE EDICIÓN ---
    if (showEditDialog && user != null) {
        EditProfileDialog(
            currentBio = user!!.bio ?: "",
            currentStatus = user!!.status ?: "",
            onDismiss = { showEditDialog = false },
            onSave = { newBio, newStatus ->
                scope.launch {
                    // Guardamos en Supabase
                    UserRepository.actualizarPerfil(user!!.id, newBio, newStatus)
                    // Recargamos la pantalla para ver los cambios
                    cargarDatos()
                    showEditDialog = false
                }
            }
        )
    }
}

// Componente para el Diálogo de Edición
@Composable
fun EditProfileDialog(
    currentBio: String,
    currentStatus: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var bio by remember { mutableStateOf(currentBio) }
    var status by remember { mutableStateOf(currentStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Editar Perfil", color = Color.White) },
        text = {
            Column {
                // Campo STATUS (Lema)
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Estado / Lema (Corto)") },
                    placeholder = { Text("Ej: Mishima Player") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFD32F2F)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo BIO
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Biografía") },
                    placeholder = { Text("Cuéntanos sobre ti...") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFD32F2F)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(bio, status) }) {
                Text("GUARDAR", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        }
    )
}

// Componente auxiliar de estadísticas (igual que antes)
@Composable
fun ProfileStat(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        Text(text = label, fontSize = 10.sp, color = Color.Gray, letterSpacing = 1.sp)
    }
}