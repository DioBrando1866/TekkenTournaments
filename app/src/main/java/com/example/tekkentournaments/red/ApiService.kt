package com.example.tekkentournaments.red

import com.example.tekkentournaments.model.CharacterData
import retrofit2.http.GET
import retrofit2.http.Url

interface TekkenApiService {
    // Usamos @Url para poder pasarle cualquier link de GitHub directamente
    @GET
    suspend fun downloadFrameData(@Url fileUrl: String): List<CharacterData>
}