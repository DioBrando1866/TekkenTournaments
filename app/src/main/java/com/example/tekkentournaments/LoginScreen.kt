package com.example.tekkentournaments

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tekkentournaments.repositories.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212) // Fondo Oscuro
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // LOGO o TÍTULO
            Text(
                text = "IRON FIST",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFD32F2F), // Rojo
                letterSpacing = 4.sp
            )
            Text(
                text = "TOURNAMENTS",
                fontSize = 16.sp,
                color = Color.LightGray,
                letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // CAMPOS DE TEXTO
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFD32F2F),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFD32F2F),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage!!, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN LOGIN
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val success = AuthRepository.login(email, password)
                        isLoading = false
                        if (success) {
                            onLoginSuccess()
                        } else {
                            errorMessage = "Credenciales incorrectas o error de conexión."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("ENTRAR AL TORNEO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("¿No tienes cuenta? Regístrate aquí", color = Color.Gray)
            }
        }
    }
}