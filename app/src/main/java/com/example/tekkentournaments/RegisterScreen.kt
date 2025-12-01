package com.example.tekkentournaments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tekkentournaments.repositories.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("NUEVO LUCHADOR", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(modifier = Modifier.height(32.dp))

            // USERNAME
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de Usuario") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // PASSWORD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña (min 6 chars)") },
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F), unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage!!, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if(password.length < 6) {
                        errorMessage = "La contraseña debe tener 6 caracteres."
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val success = AuthRepository.register(email, password, username)
                        isLoading = false
                        if (success) {
                            onRegisterSuccess()
                        } else {
                            errorMessage = "Error al registrar. Revisa el correo o la conexión."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if(isLoading) CircularProgressIndicator(color = Color.White) else Text("REGISTRARSE")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNavigateBack) { Text("Volver al Login", color = Color.Gray) }
        }
    }
}