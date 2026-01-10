package com.example.tekkentournaments.utils

import androidx.compose.ui.graphics.Color

data class CharacterColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val name: String
)

object ThemeUtils {

    val DefaultTheme = CharacterColors(
        primary = Color(0xFFD32F2F),
        secondary = Color(0xFFE53935),
        background = Color(0xFF121212),
        name = "Mishima Red"
    )

    fun getColorsForCharacter(charName: String?): CharacterColors {
        if (charName == null) return DefaultTheme

        val key = charName.lowercase().replace(" ", "")

        return when {
            key.contains("kazuya") || key.contains("reina") || key.contains("devil") -> CharacterColors(
                primary = Color(0xFF9C27B0),
                secondary = Color(0xFFE040FB),
                background = Color(0xFF1A0520),
                name = "Devil Gene"
            )

            key.contains("jin") || key.contains("lars") -> DefaultTheme

            key.contains("king") || key.contains("eddy") -> CharacterColors(
                primary = Color(0xFFFFD700), // Oro
                secondary = Color(0xFFFFEA00), // Amarillo brillante
                background = Color(0xFF1A1500), // Fondo dorado oscuro
                name = "Jaguar Gold"
            )

            key.contains("bryan") || key.contains("dragunov") || key.contains("jack") -> CharacterColors(
                primary = Color(0xFF2E7D32),
                secondary = Color(0xFF00E676),
                background = Color(0xFF051405),
                name = "War Zone"
            )

            key.contains("lili") || key.contains("asuka") || key.contains("alisa") || key.contains("xiaoyu") -> CharacterColors(
                primary = Color(0xFFE91E63),
                secondary = Color(0xFFFF4081),
                background = Color(0xFF1F0A10),
                name = "Pop Star"
            )

            key.contains("claudio") || key.contains("lee") || key.contains("steve") -> CharacterColors(
                primary = Color(0xFF2962FF),
                secondary = Color(0xFF448AFF),
                background = Color(0xFF050A19),
                name = "Excellént"
            )

            else -> DefaultTheme
        }
    }
}