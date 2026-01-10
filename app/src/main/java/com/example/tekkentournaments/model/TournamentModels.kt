package com.example.tekkentournaments.model

enum class MatchType(val label: String, val totalGames: Int, val isFirstTo: Boolean) {
    BO3("Best of 3", 3, false),
    BO5("Best of 5", 5, false),
    FT5("First to 5", 5, true),
    FT10("First to 10", 10, true);

    val winsNeeded: Int
        get() = if (isFirstTo) totalGames else (totalGames / 2) + 1
}

enum class BracketType { WINNERS, LOSERS, GRAND_FINALS }

data class TournamentMatch(
    val id: String,
    val roundIndex: Int,
    val bracketType: BracketType,
    val matchType: MatchType,

    val player1: String? = null,
    val player2: String? = null,
    val p1Score: Int = 0,
    val p2Score: Int = 0,
    val winner: String? = null,

    val nextMatchIdForWinner: String? = null,
    val nextMatchIdForLoser: String? = null
) {
    fun isReadyToPlay(): Boolean = player1 != null && player2 != null
    fun isFinished(): Boolean = winner != null
}