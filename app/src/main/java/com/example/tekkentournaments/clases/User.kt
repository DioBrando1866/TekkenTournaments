package com.example.tekkentournaments.clases

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    @SerialName("profile_image") val profileImage: String? = null,
    val bio: String? = null,
    val status: String? = null, // Ej: "Buscando partida", "Offline"
    @SerialName("created_at") val createdAt: String? = null
)