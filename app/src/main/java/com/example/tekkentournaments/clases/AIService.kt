package com.example.tekkentournaments.clases

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable // Importante para las clases de abajo
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AIService {

    // ⚠️ PEGA TU API KEY DE GOOGLE AQUÍ (AIza...)
    private const val API_KEY = "AIzaSyBJFZsBm5a02DkxZZviTzwktrSCF5h-DWU"

    // URL Correcta de Gemini 1.5 Flash
    private const val URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                prettyPrint = true
            })
        }
    }

    suspend fun obtenerConsejoTactico(p1Name: String, char1: String, p2Name: String, char2: String): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("GEMINI_DEBUG", "Enviando a Gemini...")

                val promptText = """
                    Eres un entrenador de Tekken 8.
                    Dime 3 consejos breves para ganar con $char1 contra $char2.
                    Usa emojis.
                """.trimIndent()

                // Creamos el objeto de petición usando las clases de abajo
                val requestBody = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = promptText))))
                )

                val response = client.post("$URL?key=$API_KEY") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

                val rawBody = response.bodyAsText()
                Log.d("GEMINI_DEBUG", "Status: ${response.status}")

                if (response.status == HttpStatusCode.OK) {
                    try {
                        val data = Json { ignoreUnknownKeys = true }.decodeFromString<GeminiResponse>(rawBody)
                        val texto = data.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        return@withContext texto ?: "IA sin respuesta."
                    } catch (e: Exception) {
                        return@withContext "Error leyendo respuesta."
                    }
                } else if (response.status == HttpStatusCode.NotFound) {
                    // Si sale esto, ES QUE NO HAS ACTIVADO LA API EN LA CONSOLA DE GOOGLE
                    return@withContext "❌ ERROR 404: Activa la API 'Generative Language' en Google Cloud Console."
                } else {
                    return@withContext "Error: ${response.status}"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "Error de conexión."
            }
        }
    }
}

// ==========================================
// CLASES DE DATOS (MODELOS) - ¡ESTO ES LO QUE TE FALTABA!
// ==========================================

@Serializable
data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)