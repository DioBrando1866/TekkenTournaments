package com.example.tekkentournaments.model

// 1. Tipos de Lucha: Define las reglas de victoria
enum class MatchType(val label: String, val totalGames: Int, val isFirstTo: Boolean) {
    BO3("Best of 3", 3, false),    // Gana con 2
    BO5("Best of 5", 5, false),    // Gana con 3
    FT5("First to 5", 5, true),    // Gana con 5
    FT10("First to 10", 10, true); // Gana con 10

    val winsNeeded: Int
        get() = if (isFirstTo) totalGames else (totalGames / 2) + 1
}

enum class BracketType { WINNERS, LOSERS, GRAND_FINALS }

// 2. La Partida (El nodo del grafo)
data class TournamentMatch(
    val id: String,
    val roundIndex: Int,       // 0 = Ronda 1, 1 = Semis...
    val bracketType: BracketType,
    val matchType: MatchType,  // Cada partida sabe si es BO3 o FT5

    // Jugadores y Estado
    val player1: String? = null,
    val player2: String? = null,
    val p1Score: Int = 0,
    val p2Score: Int = 0,
    val winner: String? = null,

    // Conexiones del Grafo (A dónde van)
    val nextMatchIdForWinner: String? = null,
    val nextMatchIdForLoser: String? = null // Clave para Double Elimination
) {
    fun isReadyToPlay(): Boolean = player1 != null && player2 != null
    fun isFinished(): Boolean = winner != null
}