package com.example.tekkentournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tekkentournaments.model.CharacterData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrameDataScreen(
    onBack: () -> Unit
) {
    // 1. ESTADOS
    var searchQuery by remember { mutableStateOf("") }
    var characterList by remember { mutableStateOf<List<CharacterData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 2. CARGA DE DATOS (Usando tu lógica de Red)
    LaunchedEffect(Unit) {
        try {
            // Aquí llamarías a tu repositorio o servicio que creamos
            // Por ahora, si no tienes el JSON online, puedes usar datos de prueba
            // characterList = tuRepositorio.getFrames()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text("FRAME DATA", fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Color(0xFFD32F2F))
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1E1E1E))
                )

                // Buscador con el estilo de HomeScreen
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E)).padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar luchador...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFD32F2F)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        // ESTA ES LA PARTE CORREGIDA:
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color(0xFFD32F2F),     // Rojo Tekken
                            unfocusedBorderColor = Color(0xFF333333),   // Gris oscuro
                            cursorColor = Color(0xFFD32F2F),
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // Decoración roja de la HomeScreen
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(
                Brush.horizontalGradient(colors = listOf(Color(0xFFD32F2F), Color(0xFF1E1E1E)))
            ))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD32F2F))
                }
            } else if (errorMessage != null) {
                Text("Error: $errorMessage", color = Color.Red, modifier = Modifier.padding(16.dp))
            } else {
                // 3. FILTRADO Y LISTA (Usando CharacterData)
                val filteredList = characterList.filter { it.name.contains(searchQuery, ignoreCase = true) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredList) { character ->
                        CharacterFrameCard(character)
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterFrameCard(character: CharacterData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = character.name.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. MOVIMIENTOS (Usando Move de tu TekkenModels)
            character.moves.forEach { move ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFF252525), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(move.command, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("STARTUP: ${move.startup}", color = Color.Gray, fontSize = 10.sp)
                    }

                    // Lógica de color para el bloqueo
                    val blockColor = when {
                        move.onBlock.contains("+") -> Color.Green
                        (move.onBlock.toIntOrNull() ?: 0) <= -10 -> Color(0xFFD32F2F)
                        else -> Color.White
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("BLOCK", color = Color.Gray, fontSize = 9.sp)
                        Text(move.onBlock, color = blockColor, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}