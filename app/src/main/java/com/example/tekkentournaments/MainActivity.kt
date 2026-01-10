package com.example.tekkentournaments

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.tekkentournaments.ui.theme.TekkenTournamentsTheme
import com.example.tekkentournaments.utils.LanguageUtils

enum class AppScreen {
    Splash, Login, Register, Home, Profile, TournamentsList, TournamentDetail, FrameData
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LanguageUtils.loadLocale(this)

        setContent {
            TekkenTournamentsTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

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
                            onNavigateToProfile = { currentScreen = AppScreen.Profile },
                            onNavigateToFrameData = { currentScreen = AppScreen.FrameData }
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
                                selectedTournamentId = torneoId
                                currentScreen = AppScreen.TournamentDetail
                            }
                        )
                    }

                    AppScreen.TournamentDetail -> {
                        if (selectedTournamentId != null) {
                            TournamentDetailScreen(
                                tournamentId = selectedTournamentId!!,
                                onBack = {
                                    currentScreen = AppScreen.TournamentsList
                                },
                                onTournamentDeleted = {
                                    currentScreen = AppScreen.TournamentsList
                                    selectedTournamentId = null // Limpiamos la selección
                                }
                            )
                        } else {
                            currentScreen = AppScreen.TournamentsList
                        }
                    }
                    AppScreen.FrameData -> {
                        FrameDataScreen(
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                }
            }
        }
    }
}