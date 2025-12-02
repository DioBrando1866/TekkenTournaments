package com.example.tekkentournaments.clases

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val id: String,

    @SerialName("tournament_id")
    val tournamentId: String,

    val round: Int,

    @SerialName("player1_id")
    val player1Id: String?,

    @SerialName("player2_id")
    val player2Id: String?,

    @SerialName("winner_id")
    val winnerId: String? = null,

    @SerialName("player1_score")
    val player1Score: Int = 0,

    @SerialName("player2_score")
    val player2Score: Int = 0,

    // --- AQUÍ ESTABA EL ERROR ---
    @SerialName("max_score")
    val maxScore: Int = 3
)