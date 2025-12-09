package com.example.tekkentournaments.clases

import kotlinx.serialization.Serializable

// LO QUE ENVIAMOS
@Serializable
data class HFRequest(
    val inputs: String,
    val parameters: HFParameters = HFParameters()
)

@Serializable
data class HFParameters(
    val max_new_tokens: Int = 250, // Limitar longitud respuesta
    val return_full_text: Boolean = false,
    val temperature: Double = 0.7 // Creatividad (0.0 a 1.0)
)

// LO QUE RECIBIMOS (Es una lista)
@Serializable
data class HFResponse(
    val generated_text: String
)