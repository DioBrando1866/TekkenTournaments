package com.example.tekkentournaments.clases

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AIService {

    // TU CLAVE REAL DE OPENROUTER (sk-or-v1...)
    private const val API_KEY = "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"

    private const val URL = "https://openrouter.ai/api/v1/chat/completions"

    // HE REORDENADO LA LISTA: Ponemos primero los modelos más "inteligentes" (70B/R1)
    // para que la data técnica sea más precisa, aunque tarden 1 segundo más.
    private val FREE_MODELS = listOf(
        "deepseek/deepseek-r1:free",             // El mejor para razonamiento técnico ahora mismo
        "meta-llama/llama-3.3-70b-instruct:free", // Muy bueno con datos específicos
        "qwen/qwen-2.5-vl-72b-instruct:free",    // Modelo gigante con mucho conocimiento
        "google/gemini-2.0-flash-exp:free",      // Rápido
        "google/gemini-2.0-flash-thinking-exp:free"
    )

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                prettyPrint = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 45000 // Aumentamos tiempo porque DeepSeek piensa más
            connectTimeoutMillis = 15000
        }
    }

    suspend fun obtenerConsejoTactico(miPersonaje: String, rivalPersonaje: String, juego: String = "Tekken"): String {
        return withContext(Dispatchers.IO) {

            // --- PROMPT DE INGENIERÍA PARA TEKKEN ---
            // Le damos instrucciones muy estrictas sobre cómo hablar
            val promptSystem = """
                Eres un analista profesional de Frame Data y coach de $juego.
                Tu objetivo es dar consejos técnicos de alto nivel para torneos.
                
                REGLAS OBLIGATORIAS:
                1. Usa SIEMPRE la Notación Estándar de Tekken (1, 2, 3, 4, f, b, d, u, df, qcf, etc.).
                2. Menciona inputs específicos de los ataques clave del rival.
                3. Indica los frames de castigo cuando sea relevante (ej: "es -14 en bloqueo").
                4. No des consejos genéricos como "juega defensivo". Di QUÉ castigar y CÓMO.
                5. Responde en Español, pero mantén la notación técnica en inglés/universal.
                6. Usa emojis para resaltar (🛡️, 👊, ⚠️).
            """.trimIndent()

            val promptUser = """
                Soy main $miPersonaje.
                Mi oponente usa a $rivalPersonaje.
                
                Dime 3 claves técnicas para ganar este matchup. Incluye Punishers específicos y Duckable Strings.
            """.trimIndent()

            val requestBodyBase = OpenAIRequest(
                model = "",
                messages = listOf(
                    Message(role = "system", content = promptSystem),
                    Message(role = "user", content = promptUser)
                )
            )

            // --- BUCLE DE INTENTOS ---
            for (modelId in FREE_MODELS) {
                try {
                    Log.d("AI_DEBUG", "Consultando al experto técnico: $modelId")

                    val requestBody = requestBodyBase.copy(model = modelId)
                    val response = client.post(URL) {
                        header("Authorization", "Bearer $API_KEY")
                        header("HTTP-Referer", "https://github.com/TekkenApp")
                        header("X-Title", "TekkenApp")
                        contentType(ContentType.Application.Json)
                        setBody(requestBody)
                    }

                    val rawBody = response.bodyAsText()

                    if (response.status == HttpStatusCode.OK) {
                        val data = Json { ignoreUnknownKeys = true }.decodeFromString<OpenAIResponse>(rawBody)
                        var contenido = data.choices?.firstOrNull()?.message?.content

                        if (!contenido.isNullOrBlank()) {
                            // Limpieza extra para DeepSeek (a veces suelta sus pensamientos <think>...</think>)
                            if (contenido.contains("</think>")) {
                                contenido = contenido.substringAfter("</think>").trim()
                            }

                            Log.d("AI_DEBUG", "¡Consejo técnico recibido!")
                            return@withContext contenido
                        }
                    } else {
                        Log.w("AI_DEBUG", "Fallo técnico con $modelId. Status: ${response.status}")
                    }
                } catch (e: Exception) {
                    Log.e("AI_DEBUG", "Error de conexión: ${e.message}")
                }
            }

            return@withContext "⚠️ Los analistas están ocupados. Inténtalo en un momento."
        }
    }
}