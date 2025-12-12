package com.example.tekkentournaments.utils

import androidx.compose.ui.graphics.Color

data class CharacterColors(
    val primary: Color,     // Color principal (Botones, Iconos)
    val secondary: Color,   // Color secundario (Bordes, detalles)
    val background: Color,  // Fondo (opcional, si quieres cambiar el negro puro)
    val name: String        // Nombre del tema (para debug)
)

object ThemeUtils {

    // Color por defecto (Tekken Rojo)
    val DefaultTheme = CharacterColors(
        primary = Color(0xFFD32F2F), // Rojo Tekken
        secondary = Color(0xFFE53935),
        background = Color(0xFF121212),
        name = "Mishima Red"
    )

    fun getColorsForCharacter(charName: String?): CharacterColors {
        if (charName == null) return DefaultTheme

        val key = charName.lowercase().replace(" ", "")

        return when {
            // KAZUYA / REINA (Morado Eléctrico)
            key.contains("kazuya") || key.contains("reina") || key.contains("devil") -> CharacterColors(
                primary = Color(0xFF9C27B0), // Morado
                secondary = Color(0xFFE040FB), // Neon Purple
                background = Color(0xFF1A0520), // Fondo morado muy oscuro
                name = "Devil Gene"
            )

            // JIN / LARS (Rojo/Negro/Blanco - Estilo Clásico)
            key.contains("jin") || key.contains("lars") -> DefaultTheme

            // KING / EDDY (Dorado/Amarillo - Lucha Libre/Capoeira)
            key.contains("king") || key.contains("eddy") -> CharacterColors(
                primary = Color(0xFFFFD700), // Oro
                secondary = Color(0xFFFFEA00), // Amarillo brillante
                background = Color(0xFF1A1500), // Fondo dorado oscuro
                name = "Jaguar Gold"
            )

            // BRYAN / DRAGUNOV (Azul/Verde Militar)
            key.contains("bryan") || key.contains("dragunov") || key.contains("jack") -> CharacterColors(
                primary = Color(0xFF2E7D32), // Verde Militar
                secondary = Color(0xFF00E676), // Verde Neon
                background = Color(0xFF051405), // Fondo verdoso
                name = "War Zone"
            )

            // LILI / ASUKA / ALISA / XIAOYU (Rosa/Pastel)
            key.contains("lili") || key.contains("asuka") || key.contains("alisa") || key.contains("xiaoyu") -> CharacterColors(
                primary = Color(0xFFE91E63), // Rosa
                secondary = Color(0xFFFF4081), // Rosa Neon
                background = Color(0xFF1F0A10), // Fondo rosado oscuro
                name = "Pop Star"
            )

            // CLAUDIO / LEE (Azul Elegante)
            key.contains("claudio") || key.contains("lee") || key.contains("steve") -> CharacterColors(
                primary = Color(0xFF2962FF), // Azul Royal
                secondary = Color(0xFF448AFF), // Azul Claro
                background = Color(0xFF050A19), // Fondo azulado
                name = "Excellént"
            )

            else -> DefaultTheme
        }
    }
}