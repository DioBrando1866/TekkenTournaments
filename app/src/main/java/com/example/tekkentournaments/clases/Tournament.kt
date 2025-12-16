package com.example.tekkentournaments.clases

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tournament(
    val id: String,
    val name: String,

    val description: String? = "Sin descripción",

    val date: String? = "TBD",

    @SerialName("max_players")
    val maxPlayers: Int = 16,

    @SerialName("tournament_type")
    val tournamentType: String? = "Eliminación Simple",

    @SerialName("creator_id")
    val creatorId: String? = null,

    @SerialName("creator_name")
    val creatorName: String? = "Desconocido",

    @SerialName("is_public")
    val isPublic: Boolean = true,

    @SerialName("game_version")
    val gameVersion: String? = "Tekken 8",

    // Estado del torneo
    val status: String? = "Abierto"
)