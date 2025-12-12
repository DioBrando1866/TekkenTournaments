package com.example.tekkentournaments.repositories

import android.util.Log
import com.example.tekkentournaments.clases.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import supabase
import io.github.jan.supabase.storage.storage

object UserRepository {

    suspend fun obtenerUsuarios(): List<User> {
        return try {
            val listaUsuarios = supabase.from("users")
                .select()
                .decodeList<User>()

            Log.d("REPO", "Éxito: ${listaUsuarios.size} usuarios cargados")
            listaUsuarios
        } catch (e: Exception) {
            Log.e("REPO", "Error en repositorio: ${e.message}")
            emptyList()
        }
    }

    suspend fun obtenerMiPerfil(): User? {
        val authUser = supabase.auth.currentUserOrNull()
        val userId = authUser?.id ?: return null

        return try {
            supabase.from("users").select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<User>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Esta función antigua la podemos mantener por compatibilidad,
    // pero idealmente usaremos la 'Completo' de abajo.
    suspend fun actualizarPerfil(userId: String, nuevoBio: String, nuevoStatus: String) {
        try {
            supabase.from("users").update(
                {
                    set("bio", nuevoBio)
                    set("status", nuevoStatus)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun subirAvatar(userId: String, byteArray: ByteArray): String {
        val bucket = supabase.storage.from("avatars")
        val fileName = "$userId-${System.currentTimeMillis()}.jpg"

        bucket.upload(fileName, byteArray) {
            upsert = true
        }
        return bucket.publicUrl(fileName)
    }

    // ✅ FUNCIÓN ACTUALIZADA: Ahora guarda el Personaje (Main)
    suspend fun actualizarPerfilCompleto(
        userId: String,
        nuevoUsername: String,
        nuevoBio: String,
        nuevoStatus: String,
        nuevoCharacter: String, // <--- NUEVO PARÁMETRO
        nuevaImagenUrl: String? = null
    ) {
        try {
            supabase.from("users").update(
                {
                    set("username", nuevoUsername)
                    set("bio", nuevoBio)
                    set("status", nuevoStatus)
                    set("character_main", nuevoCharacter) // <--- GUARDAMOS EN SUPABASE

                    if (nuevaImagenUrl != null) {
                        set("profile_image", nuevaImagenUrl)
                    }
                }
            ) {
                filter { eq("id", userId) }
            }
        } catch (e: Exception) {
            Log.e("REPO", "Error al actualizar perfil completo: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun debugSumarVictorias(userId: String, victoriasActuales: Int) {
        try {
            val nuevasWins = victoriasActuales + 10
            supabase.from("users").update({
                set("wins", nuevasWins)
            }) {
                filter { eq("id", userId) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}