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
import supabase // Tu cliente Supabase global

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

    suspend fun crearTorneo(nombre: String, descripcion: String, fecha: String, maxJugadores: Int, tipo: String): Boolean {
        return try {
            val currentUser = supabase.auth.currentUserOrNull() ?: return false

            // Intentamos obtener el nombre del creador
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

    suspend fun agregarJugador(tournamentId: String, nombre: String): Boolean {
        return try {
            // Nota: Aquí podrías buscar si existe un usuario con ese nombre para guardar su ID real
            val player = Player(
                id = java.util.UUID.randomUUID().toString(),
                name = nombre,
                tournamentId = tournamentId
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

            // Crear parejas (Ronda 1)
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

            // Insertar matches (uno a uno para seguridad en debug)
            matches.forEach { supabase.from("matches").insert(it) }

            // Actualizar estado del torneo
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
            // 1. Actualizar el match en Supabase
            supabase.from("matches").update({
                set("player1_score", match.player1Score)
                set("player2_score", match.player2Score)
                set("winner_id", match.winnerId)
            }) {
                filter { eq("id", match.id) }
            }

            // 2. Si hay ganador, SUMAR VICTORIA al perfil del usuario (Gamificación)
            if (match.winnerId != null) {
                incrementarVictoriaUsuario(match.winnerId)
            }

            true
        } catch (e: Exception) {
            Log.e("REPO", "Error actualizando match: ${e.message}")
            false
        }
    }

    // Función auxiliar para sumar +1 win
    private suspend fun incrementarVictoriaUsuario(playerId: String) {
        try {
            // Paso A: Obtener el nombre del jugador que ganó (desde la tabla players)
            val player = supabase.from("players").select {
                filter { eq("id", playerId) }
            }.decodeSingleOrNull<Player>() ?: return

            // Paso B: Buscar si existe un USUARIO real con ese mismo nombre
            // (Esto asume que el nombre del Player coincide con el Username, lo cual es común)
            val user = supabase.from("users").select {
                filter { eq("username", player.name) }
            }.decodeSingleOrNull<User>()

            // Paso C: Si encontramos al usuario, le sumamos 1 victoria
            if (user != null) {
                supabase.from("users").update({
                    set("wins", user.wins + 1)
                }) {
                    filter { eq("id", user.id) }
                }
                Log.d("REPO", "¡Victoria sumada a ${user.username}! Total: ${user.wins + 1}")
            }
        } catch (e: Exception) {
            Log.e("REPO", "No se pudo sumar victoria al usuario: ${e.message}")
            // No bloqueamos el flujo principal si esto falla
        }
    }
}