package com.example.tekkentournaments.repositories

import android.util.Log
import com.example.tekkentournaments.clases.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import supabase
import io.github.jan.supabase.storage.storage

// Creamos un 'object' para agrupar las funciones de base de datos
object UserRepository {

    // Cambiamos el nombre a algo más útil y hacemos que devuelva List<User>
    suspend fun obtenerUsuarios(): List<User> {
        return try {
            val listaUsuarios = supabase.from("users") // Usa tu variable supabase
                .select()
                .decodeList<User>()

            Log.d("REPO", "Éxito: ${listaUsuarios.size} usuarios cargados")
            listaUsuarios // Devolvemos la lista
        } catch (e: Exception) {
            Log.e("REPO", "Error en repositorio: ${e.message}")
            emptyList() // Si falla, devolvemos lista vacía para no romper la app
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

    suspend fun actualizarPerfil(userId: String, nuevoBio: String, nuevoStatus: String) {
        try {
            // Actualizamos solo las columnas necesarias
            // Usamos un Map o un objeto anónimo para actualizar parcialmente
            // Nota: En Supabase-kt, la forma más fácil para actualizaciones parciales
            // es usar el DSL de update con 'set'.

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
            throw e // Lanzamos el error para que la UI sepa que falló
        }
    }
    suspend fun subirAvatar(userId: String, byteArray: ByteArray): String {
        val bucket = supabase.storage.from("avatars")
        val fileName = "$userId-${System.currentTimeMillis()}.jpg"

        // Subimos el archivo
        bucket.upload(fileName, byteArray) {
            upsert = true
        }

        // Obtenemos la URL pública para guardarla en la base de datos
        return bucket.publicUrl(fileName)
    }

    // 2. Función actualizada para guardar TODO
    suspend fun actualizarPerfilCompleto(
        userId: String,
        nuevoUsername: String,
        nuevoBio: String,
        nuevoStatus: String,
        nuevaImagenUrl: String? = null // Opcional, solo si cambió la foto
    ) {
        supabase.from("users").update(
            {
                set("username", nuevoUsername)
                set("bio", nuevoBio)
                set("status", nuevoStatus)
                // Solo actualizamos la imagen si nos pasan una nueva URL
                if (nuevaImagenUrl != null) {
                    set("profile_image", nuevaImagenUrl)
                }
            }
        ) {
            filter { eq("id", userId) }
        }
    }
    suspend fun debugSumarVictorias(userId: String, victoriasActuales: Int) {
        try {
            // Sumamos 10 victorias de golpe para saltar de rango rápido
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