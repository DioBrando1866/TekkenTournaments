package com.example.tekkentournaments.clases

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("is_winner") val isWinner: Boolean = false
)