package com.example.tekkentournaments.clases

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
    @SerialName("tournament_id") val tournamentId: String,
    val is_winner: Boolean = false,
    @SerialName("character_main") val characterMain: String = "Random"
)