package com.example.tekkentournaments.repositories

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

// Imports de tus clases y cliente supabase
import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.clases.Player
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.clases.User
import io.github.jan.supabase.auth.auth
import supabase

object TournamentRepository {

    // --- 1. GESTIÓN DE TORNEOS ---

    suspend fun obtenerTorneos(): List<Tournament> {
        return try {
            supabase.from("tournaments")
                .select {
                    order(column = "date", order = Order.DESCENDING)
                }
                .decodeList<Tournament>()
        } catch (e: Exception) {
            Log.e("REPO", "Error obtenerTorneos: ${e.message}")
            emptyList()
        }
    }

    suspend fun obtenerTorneoPorId(id: String): Tournament? {
        return try {
            supabase.from("tournaments").select {
                filter { eq("id", id) }
            }.decodeSingleOrNull<Tournament>()
        } catch (e: Exception) {
            null
        }
    }

    // ACTUALIZADO: AHORA RECIBE EL JUEGO
    suspend fun crearTorneo(nombre: String, descripcion: String, fecha: String, maxJugadores: Int, tipo: String, juego: String): Boolean {
        return try {
            val currentUser = supabase.auth.currentUserOrNull() ?: return false

            val userProfile = supabase.from("users").select {
                filter { eq("id", currentUser.id) }
            }.decodeSingleOrNull<User>()
            val creatorName = userProfile?.username ?: "Desconocido"

            val nuevoTorneo = Tournament(
                id = java.util.UUID.randomUUID().toString(),
                name = nombre,
                description = descripcion,
                date = fecha,
                maxPlayers = maxJugadores,
                tournamentType = tipo,
                gameVersion = juego, // <--- GUARDAMOS LA VERSIÓN DEL JUEGO
                creatorId = currentUser.id,
                creatorName = creatorName,
                isPublic = true
            )

            supabase.from("tournaments").insert(nuevoTorneo)
            true
        } catch (e: Exception) {
            Log.e("REPO", "Error crearTorneo: ${e.message}")
            false
        }
    }

    suspend fun editarTorneo(id: String, name: String, desc: String, date: String, maxPlayers: Int): Boolean {
        return try {
            supabase.from("tournaments").update({
                set("name", name)
                set("description", desc)
                set("date", date)
                set("max_players", maxPlayers)
            }) {
                filter { eq("id", id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun borrarTorneo(id: String): Boolean {
        return try {
            supabase.from("tournaments").delete {
                filter { eq("id", id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- 2. GESTIÓN DE JUGADORES ---

    // ESTA ES LA VERSIÓN CORRECTA QUE INCLUYE PERSONAJE
    suspend fun agregarJugador(tournamentId: String, nombre: String, personaje: String): Boolean {
        return try {
            val player = Player(
                id = java.util.UUID.randomUUID().toString(),
                name = nombre,
                tournamentId = tournamentId,
                characterMain = personaje
            )
            supabase.from("players").insert(player)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerJugadores(tournamentId: String): List<Player> {
        return try {
            supabase.from("players").select {
                filter { eq("tournament_id", tournamentId) }
            }.decodeList<Player>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- 3. GESTIÓN DE MATCHES Y BRACKET ---

    suspend fun generarBracketInicial(tournamentId: String, formato: Int): Boolean {
        return try {
            val players = obtenerJugadores(tournamentId)
            if (players.size < 2) {
                Log.e("REPO", "Mínimo 2 jugadores")
                return false
            }

            val shuffled = players.shuffled()
            val matches = mutableListOf<Match>()

            for (i in shuffled.indices step 2) {
                if (i + 1 < shuffled.size) {
                    val p1 = shuffled[i]
                    val p2 = shuffled[i+1]
                    matches.add(
                        Match(
                            id = java.util.UUID.randomUUID().toString(),
                            tournamentId = tournamentId,
                            round = 1,
                            player1Id = p1.id,
                            player2Id = p2.id,
                            winnerId = null,
                            player1Score = 0,
                            player2Score = 0,
                            maxScore = formato
                        )
                    )
                }
            }

            matches.forEach { supabase.from("matches").insert(it) }

            supabase.from("tournaments").update({
                set("status", "en_curso")
            }) {
                filter { eq("id", tournamentId) }
            }
            true
        } catch (e: Exception) {
            Log.e("REPO", "Error generando bracket: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerMatches(tournamentId: String): List<Match> {
        return try {
            supabase.from("matches").select {
                filter { eq("tournament_id", tournamentId) }
                order("round", Order.ASCENDING)
            }.decodeList<Match>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- 4. ACTUALIZAR RESULTADO Y GAMIFICACIÓN ---

    suspend fun actualizarPartida(match: Match): Boolean {
        return try {
            supabase.from("matches").update({
                set("player1_score", match.player1Score)
                set("player2_score", match.player2Score)
                set("winner_id", match.winnerId)
            }) {
                filter { eq("id", match.id) }
            }

            if (match.winnerId != null) {
                incrementarVictoriaUsuario(match.winnerId)
            }

            true
        } catch (e: Exception) {
            Log.e("REPO", "Error actualizando match: ${e.message}")
            false
        }
    }

    private suspend fun incrementarVictoriaUsuario(playerId: String) {
        try {
            val player = supabase.from("players").select {
                filter { eq("id", playerId) }
            }.decodeSingleOrNull<Player>() ?: return

            val user = supabase.from("users").select {
                filter { eq("username", player.name) }
            }.decodeSingleOrNull<User>()

            if (user != null) {
                supabase.from("users").update({
                    set("wins", user.wins + 1)
                }) {
                    filter { eq("id", user.id) }
                }
            }
        } catch (e: Exception) {
            Log.e("REPO", "No se pudo sumar victoria al usuario: ${e.message}")
        }
    }
}