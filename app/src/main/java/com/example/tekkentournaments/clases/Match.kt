package com.example.tekkentournaments.clases

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val id: String,
    @SerialName("tournament_id") val tournamentId: String,
    val round: Int,
    @SerialName("player1_id") val player1Id: String?,
    @SerialName("player2_id") val player2Id: String?,
    @SerialName("winner_id") val winnerId: String?,
    @SerialName("player1_score") val player1Score: Int,
    @SerialName("player2_score") val player2Score: Int
)