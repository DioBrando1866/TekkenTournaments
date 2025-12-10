package com.example.tekkentournaments

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.tekkentournaments.ui.theme.TekkenTournamentsTheme

// 1. AÑADIMOS LA PANTALLA DE DETALLE AL ENUM
enum class AppScreen {
    Splash, Login, Register, Home, Profile, TournamentsList, TournamentDetail // <--- NUEVO
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TekkenTournamentsTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

                // 2. VARIABLE PARA GUARDAR EL ID DEL TORNEO SELECCIONADO
                var selectedTournamentId by remember { mutableStateOf<String?>(null) }

                when (currentScreen) {
                    AppScreen.Splash -> {
                        SplashScreen(
                            onSessionCheckFinished = { isLoggedIn ->
                                currentScreen = if (isLoggedIn) AppScreen.Home else AppScreen.Login
                            }
                        )
                    }
                    AppScreen.Login -> {
                        LoginScreen(
                            onLoginSuccess = { currentScreen = AppScreen.Home },
                            onNavigateToRegister = { currentScreen = AppScreen.Register }
                        )
                    }
                    AppScreen.Register -> {
                        RegisterScreen(
                            onRegisterSuccess = { currentScreen = AppScreen.Home },
                            onNavigateBack = { currentScreen = AppScreen.Login }
                        )
                    }
                    AppScreen.Home -> {
                        HomeScreen(
                            onNavigateToList = { currentScreen = AppScreen.TournamentsList },
                            onNavigateToProfile = { currentScreen = AppScreen.Profile }
                        )
                    }
                    AppScreen.Profile -> {
                        ProfileScreen(
                            onLogout = { currentScreen = AppScreen.Login },
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                    AppScreen.TournamentsList -> {
                        TournamentsListScreen(
                            onBack = { currentScreen = AppScreen.Home },
                            onTournamentClick = { torneoId ->
                                // 3. GUARDAMOS EL ID Y CAMBIAMOS DE PANTALLA
                                selectedTournamentId = torneoId
                                currentScreen = AppScreen.TournamentDetail
                            }
                        )
                    }

                    // 4. PANTALLA DE DETALLE
                    AppScreen.TournamentDetail -> {
                        if (selectedTournamentId != null) {
                            TournamentDetailScreen(
                                tournamentId = selectedTournamentId!!,
                                onBack = {
                                    currentScreen = AppScreen.TournamentsList
                                },
                                onTournamentDeleted = {
                                    // Si se borra, volvemos a la lista
                                    currentScreen = AppScreen.TournamentsList
                                }
                            )
                        } else {
                            // Error de seguridad por si el ID es nulo
                            currentScreen = AppScreen.TournamentsList
                        }
                    }
                }
            }
        }
    }
}