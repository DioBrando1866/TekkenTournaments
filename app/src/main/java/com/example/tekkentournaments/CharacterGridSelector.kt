package com.example.tekkentournaments

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tekkentournaments.utils.TekkenData

@Composable
fun CharacterGridSelector(
    gameVersion: String,
    selectedCharacter: String,
    onCharacterSelected: (String) -> Unit
) {
    // Obtenemos la lista de nombres
    val characters = remember(gameVersion) { TekkenData.getCharacters(gameVersion) }

    Column {
        Text(
            text = "ROSTER ($gameVersion)",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // GRID DE PERSONAJES
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 70.dp), // Se adapta al ancho
            modifier = Modifier
                .height(300.dp) // Altura fija para que haga scroll dentro del diálogo
                .background(Color(0xFF1A1A1A), MaterialTheme.shapes.medium)
                .border(1.dp, Color(0xFF333333), MaterialTheme.shapes.medium),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(characters) { charName ->
                CharacterItem(
                    name = charName,
                    isSelected = charName == selectedCharacter,
                    onClick = { onCharacterSelected(charName) }
                )
            }
        }
    }
}

@Composable
fun CharacterItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    // LLAMAMOS A LA FUNCIÓN DE LAS URLs
    val imageUrl = remember(name) { TekkenData.getCharacterImageUrl(name) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) Color(0xFFD32F2F) else Color.Gray,
                    shape = CircleShape
                )
                .padding(2.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFFD32F2F).copy(alpha = 0.2f) else Color.Transparent)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    // 👇 ESTA LÍNEA ES MÁGICA: Fingimos ser un PC para que no nos bloqueen
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .error(android.R.drawable.ic_menu_camera) // Si falla, sale cámara
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Crop, // Crop para que llene el círculo
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            color = if (isSelected) Color(0xFFD32F2F) else Color.White,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}