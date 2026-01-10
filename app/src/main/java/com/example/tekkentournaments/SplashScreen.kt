package com.example.tekkentournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tekkentournaments.repositories.AuthRepository
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSessionCheckFinished: (Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        delay(500)

        val haySesion = AuthRepository.recuperarSesion()
        onSessionCheckFinished(haySesion)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "IRON FIST",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFD32F2F),
                letterSpacing = 2.sp
            )
            Text(
                text = "LOADING...",
                fontSize = 12.sp,
                color = Color.Gray,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(color = Color(0xFFD32F2F))
        }
    }
}