package com.example.tekkentournaments.utils

import androidx.compose.ui.graphics.Color

data class TekkenRank(
    val title: String,
    val color: Color,
    val minWins: Int
)

object TekkenRankUtils {

    private val ranks = listOf(
        // --- TIER 1: BEGINNER / DANS (Plata/Madera) ---
        TekkenRank("BEGINNER", Color(0xFF8D6E63), 0),
        TekkenRank("1ST DAN", Color(0xFFB0BEC5), 3),
        TekkenRank("2ND DAN", Color(0xFFCFD8DC), 6),

        // --- TIER 2: LIGHT BLUE (Cian) ---
        TekkenRank("FIGHTER", Color(0xFF00E5FF), 10),
        TekkenRank("STRATEGIST", Color(0xFF00B8D4), 14),
        TekkenRank("COMBATANT", Color(0xFF00ACC1), 18),

        // --- TIER 3: GREEN (Verde) ---
        TekkenRank("BRAWLER", Color(0xFF76FF03), 25),
        TekkenRank("RANGER", Color(0xFF64DD17), 30),
        TekkenRank("CAVALRY", Color(0xFF43A047), 35),

        // --- TIER 4: YELLOW (Amarillo) ---
        TekkenRank("WARRIOR", Color(0xFFFFD600), 45),
        TekkenRank("ASSAILANT", Color(0xFFFFC400), 50),
        TekkenRank("DOMINATOR", Color(0xFFFFAB00), 55),

        // --- TIER 5: ORANGE (Naranja) ---
        TekkenRank("VANQUISHER", Color(0xFFFF6D00), 65),
        TekkenRank("DESTROYER", Color(0xFFE65100), 75),
        TekkenRank("ELIMINATOR", Color(0xFFBF360C), 85),

        // --- TIER 6: RED (Rojo - Garyu) ---
        TekkenRank("GARYU", Color(0xFFD50000), 100),
        TekkenRank("SHINRYU", Color(0xFFB71C1C), 115),
        TekkenRank("TENRYU", Color(0xFF8B0000), 130),

        // --- TIER 7: PURPLE (Morado - Ruler) ---
        TekkenRank("MIGHTY RULER", Color(0xFFAA00FF), 150),
        TekkenRank("FLAME RULER", Color(0xFF7B1FA2), 170),
        TekkenRank("BATTLE RULER", Color(0xFF4A148C), 190),

        // --- TIER 8: BLUE (Azul - Fujin) ---
        TekkenRank("FUJIN", Color(0xFF2962FF), 220),
        TekkenRank("RAIJIN", Color(0xFF0091EA), 250),
        TekkenRank("KISHIN", Color(0xFF01579B), 280),
        TekkenRank("BUSHIN", Color(0xFF1A237E), 310),

        // --- TIER 9: GOLD/EMPEROR (Dorado Real) ---
        TekkenRank("TEKKEN KING", Color(0xFFFFAB40), 350),
        TekkenRank("TEKKEN EMPEROR", Color(0xFFFFD740), 400),

        // --- TIER 10: GOD (Dorado Brillante) ---
        TekkenRank("TEKKEN GOD", Color(0xFFFFEA00), 450),
        TekkenRank("TEKKEN GOD SUPREME", Color(0xFFFFD700), 500),

        // --- TIER 11: DESTRUCTION (Rosa Neon / Final) ---
        TekkenRank("GOD OF DESTRUCTION", Color(0xFFFF00CC), 600)
    )

    fun getRankFromWins(wins: Int): TekkenRank {
        return ranks.lastOrNull { wins >= it.minWins } ?: ranks.first()
    }

    fun getNextRank(wins: Int): TekkenRank? {
        return ranks.firstOrNull { it.minWins > wins }
    }

    fun getProgressToNextRank(wins: Int): Float {
        val current = getRankFromWins(wins)
        val next = getNextRank(wins) ?: return 1f

        val winsInThisRank = wins - current.minWins
        val winsNeededForNext = next.minWins - current.minWins

        return winsInThisRank.toFloat() / winsNeededForNext.toFloat()
    }
}