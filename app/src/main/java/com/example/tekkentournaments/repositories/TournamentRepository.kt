package com.example.tekkentournaments.repositories

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.auth.auth

// Imports de tus clases y cliente supabase
import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.clases.Player
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.clases.User
import supabase

object TournamentRepository {

    // ==========================================
    // 1. GESTIÓN DE TORNEOS (CRUD)
    // ==========================================

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
            Log.e("REPO", "Error obtenerTorneoPorId: ${e.message}")
            null
        }
    }

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
                gameVersion = juego,
                creatorId = currentUser.id,
                creatorName = creatorName,
                status = "open",
                isPublic = true
                // participants eliminado correctamente
            )

            supabase.from("tournaments").insert(nuevoTorneo)
            true
        } catch (e: Exception) {
            Log.e("REPO", "Error crearTorneo: ${e.message}")
            false
        }
    }

    suspend fun eliminarTorneo(id: String): Boolean {
        return try {
            supabase.from("tournaments").delete {
                filter { eq("id", id) }
            }
            true
        } catch (e: Exception) {
            Log.e("REPO", "Error eliminarTorneo: ${e.message}")
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

    // ==========================================
    // 2. GESTIÓN DE JUGADORES
    // ==========================================

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
            Log.e("REPO", "Error agregarJugador: ${e.message}")
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

    // ==========================================
    // 3. GESTIÓN DE MATCHES Y BRACKET
    // ==========================================

    // ✅ ESTA ES LA FUNCIÓN QUE FALTABA Y QUE SOLUCIONA EL ERROR
    suspend fun generarBracketInicial(tournamentId: String, formato: Int): Boolean {
        return try {
            val players = obtenerJugadores(tournamentId)
            if (players.size < 2) {
                Log.e("REPO", "Mínimo 2 jugadores para generar bracket")
                return false
            }

            val shuffled = players.shuffled()
            val matches = mutableListOf<Match>()

            // Generamos emparejamientos de la Ronda 1
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
                            maxScore = formato // Se pasa el formato (ej. 3 para BO3)
                        )
                    )
                }
            }

            // Insertamos todos los matches en Supabase
            if (matches.isNotEmpty()) {
                matches.forEach { supabase.from("matches").insert(it) }
            }

            // Actualizamos estado del torneo a "en_curso"
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

    suspend fun obtenerPartidosDelTorneo(tournamentId: String): List<Match> {
        return try {
            supabase.from("matches").select {
                filter { eq("tournament_id", tournamentId) }
                order("round", Order.ASCENDING)
                order("id", Order.ASCENDING)
            }.decodeList<Match>()
        } catch (e: Exception) {
            Log.e("REPO", "Error obtenerPartidosDelTorneo: ${e.message}")
            emptyList()
        }
    }

    suspend fun actualizarPartido(matchId: String, score1: Int, score2: Int, winnerId: String?) {
        try {
            supabase.from("matches").update({
                set("player1_score", score1)
                set("player2_score", score2)
                set("winner_id", winnerId)
            }) {
                filter { eq("id", matchId) }
            }

            // Gamificación: Si hay ganador, sumamos victoria
            if (winnerId != null) {
                incrementarVictoriaUsuario(winnerId)
            }
        } catch (e: Exception) {
            Log.e("REPO", "Error actualizarPartido: ${e.message}")
        }
    }

    // ==========================================
    // 4. GAMIFICACIÓN INTERNA
    // ==========================================

    private suspend fun incrementarVictoriaUsuario(playerId: String) {
        try {
            // 1. Buscamos al jugador para obtener su nombre real
            val player = supabase.from("players").select {
                filter { eq("id", playerId) }
            }.decodeSingleOrNull<Player>() ?: return

            // 2. Buscamos si ese nombre corresponde a un usuario registrado
            val user = supabase.from("users").select {
                filter { eq("username", player.name) }
            }.decodeSingleOrNull<User>()

            // 3. Si existe, le sumamos +1 Win
            if (user != null) {
                supabase.from("users").update({
                    set("wins", user.wins + 1)
                }) {
                    filter { eq("id", user.id) }
                }
            }
        } catch (e: Exception) {
            Log.e("REPO", "No se pudo sumar victoria: ${e.message}")
        }
    }
}