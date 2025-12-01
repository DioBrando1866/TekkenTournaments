package com.example.tekkentournaments.clases

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tournament(
    val id: String,
    val name: String,
    val date: String? = null,
    @SerialName("creator_id") val creatorId: String,
    @SerialName("creator_name") val creatorName: String? = null,
    val description: String? = null,
    val rounds: Int? = null, // Número total de rondas
    @SerialName("tournament_type") val tournamentType: String = "Double Elimination", // "Single", "Double", "Round Robin"
    @SerialName("is_public") val isPublic: Boolean = true,
    @SerialName("max_players") val maxPlayers: Int = 64,
    @SerialName("created_at") val createdAt: String? = null
)