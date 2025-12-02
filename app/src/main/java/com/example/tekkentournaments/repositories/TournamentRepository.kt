package com.example.tekkentournaments.repositories

import android.util.Log
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.clases.Player
import com.example.tekkentournaments.clases.Tournament
import com.example.tekkentournaments.clases.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import supabase

object TournamentRepository {

    suspend fun obtenerTorneos(): List<Tournament> {
        return try {
            // Descarga los torneos, ordenados por fecha (los más nuevos arriba)
            supabase.from("tournaments")
                .select {
                    order(column = "date", order = Order.DESCENDING)
                }
                .decodeList<Tournament>()
        } catch (e: Exception) {
            Log.e("TournamentRepo", "Error al cargar torneos: ${e.message}")
            emptyList()
        }
    }
    suspend fun crearTorneo(
        nombre: String,
        descripcion: String,
        fecha: String,
        maxJugadores: Int,
        tipo: String
    ): Boolean {
        return try {
            // 1. Obtenemos el usuario actual para ponerlo como Creador
            val currentUser = supabase.auth.currentUserOrNull() ?: return false

            // Opcional: Obtener el nombre del usuario para guardarlo en creator_name
            // (Si no quieres hacer esta llamada extra, puedes pasar el nombre desde la UI)
            val userProfile = supabase.from("users").select {
                filter { eq("id", currentUser.id) }
            }.decodeSingleOrNull<User>()

            val creatorName = userProfile?.username ?: "Desconocido"

            // 2. Creamos el objeto Torneo
            // Generamos ID localmente o dejamos que Supabase lo haga (aquí lo generamos para enviar el objeto completo)
            val nuevoTorneo = Tournament(
                id = java.util.UUID.randomUUID().toString(),
                name = nombre,
                description = descripcion,
                date = fecha,
                maxPlayers = maxJugadores,
                tournamentType = tipo,
                creatorId = currentUser.id,
                creatorName = creatorName,
                isPublic = true,
                createdAt = null // Supabase pondrá la fecha real
            )

            // 3. Insertar en Supabase
            supabase.from("tournaments").insert(nuevoTorneo)
            true
        } catch (e: Exception) {
            Log.e("TournamentRepo", "Error creando torneo: ${e.message}")
            false
        }
    }
    // 1. OBTENER UN SOLO TORNEO (Para la pantalla de detalle)
    suspend fun obtenerTorneoPorId(id: String): Tournament? {
        return try {
            supabase.from("tournaments").select {
                filter { eq("id", id) }
            }.decodeSingleOrNull<Tournament>()
        } catch (e: Exception) {
            null
        }
    }

    // 2. BORRAR TORNEO
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

    // 3. EDITAR TORNEO
    suspend fun editarTorneo(id: String, name: String, desc: String, date: String, maxPlayers: Int): Boolean {
        return try {
            supabase.from("tournaments").update(
                {
                    set("name", name)
                    set("description", desc)
                    set("date", date)
                    set("max_players", maxPlayers)
                }
            ) {
                filter { eq("id", id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    // 1. AÑADIR JUGADOR (Puede ser usuario o nombre suelto)
    suspend fun agregarJugador(tournamentId: String, nombre: String, userId: String? = null): Boolean {
        return try {
            val player = Player(
                id = java.util.UUID.randomUUID().toString(),
                name = nombre,
                tournamentId = tournamentId,
                // Si quieres guardar el userId real en el futuro, añádelo a la data class Player
            )
            supabase.from("players").insert(player)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 2. OBTENER JUGADORES
    suspend fun obtenerJugadores(tournamentId: String): List<Player> {
        return try {
            supabase.from("players").select {
                filter { eq("tournament_id", tournamentId) }
            }.decodeList<Player>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 3. GENERAR BRACKET (Crear Matches iniciales)
    suspend fun generarBracketInicial(tournamentId: String, formato: Int): Boolean {
        // formato: es el número de victorias necesarias (2 para Bo3, 3 para Bo5, 5 para Ft5)
        return try {
            val players = obtenerJugadores(tournamentId)
            if (players.size < 2) return false

            // Barajar aleatoriamente
            val shuffled = players.shuffled()

            // Crear matches de Ronda 1
            // Nota simplificada: Esto asume número par de jugadores.
            // Si es impar, uno pasaría directo (Bye), pero para este ejemplo haremos parejas simples.
            val matches = mutableListOf<Match>()

            for (i in shuffled.indices step 2) {
                if (i + 1 < shuffled.size) {
                    matches.add(
                        Match(
                            id = java.util.UUID.randomUUID().toString(),
                            tournamentId = tournamentId,
                            round = 1,
                            player1Id = shuffled[i].id,
                            player2Id = shuffled[i+1].id,
                            winnerId = null,
                            player1Score = 0,
                            player2Score = 0,
                            maxScore = formato // Aquí guardamos si es Bo3, Bo5, etc.
                        )
                    )
                }
            }

            supabase.from("matches").insert(matches)
            // Actualizamos estado del torneo a "En Curso"
            supabase.from("tournaments").update({ set("status", "en_curso") }) { filter { eq("id", tournamentId) } }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 4. OBTENER MATCHES
    suspend fun obtenerMatches(tournamentId: String): List<Match> {
        return try {
            supabase.from("matches").select {
                filter { eq("tournament_id", tournamentId) }
                order("round", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
            }.decodeList<Match>()
        } catch (e: Exception) {
            emptyList()
        }
    }
    // ACTUALIZAR PUNTUACIÓN Y GANADOR
    suspend fun actualizarPartida(match: Match): Boolean {
        return try {
            supabase.from("matches").update(
                {
                    set("player1_score", match.player1Score)
                    set("player2_score", match.player2Score)
                    set("winner_id", match.winnerId)
                }
            ) {
                filter { eq("id", match.id) }
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("REPO", "Error actualizando match: ${e.message}")
            false
        }
    }
}