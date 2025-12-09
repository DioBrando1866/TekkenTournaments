package com.example.tekkentournaments.clases

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tournament(
    val id: String,
    val name: String,

    // Si la descripción es null, ponemos un texto vacío
    val description: String? = "Sin descripción",

    // Si la fecha es null, ponemos "TBD" (To Be Determined)
    val date: String? = "TBD",

    @SerialName("max_players")
    val maxPlayers: Int = 16,

    // --- EL CAMPO QUE DABA ERROR ---
    // Lo hacemos nullable (?) y le damos valor por defecto
    @SerialName("tournament_type")
    val tournamentType: String? = "Eliminación Simple",

    @SerialName("creator_id")
    val creatorId: String? = null,

    @SerialName("creator_name")
    val creatorName: String? = "Desconocido",

    @SerialName("is_public")
    val isPublic: Boolean = true,

    // Versión del juego (Tekken 8, Tekken 3...)
    @SerialName("game_version")
    val gameVersion: String? = "Tekken 8", // También lo protegemos con ? por si acaso

    // Estado del torneo
    val status: String? = "Abierto"
)