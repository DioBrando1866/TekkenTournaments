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

    // Actualiza solo Bio y Status (versión simple)
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

    // Actualiza TODO el perfil (Bio, Status, Main, Foto y Banner)
    suspend fun actualizarPerfilCompleto(
        userId: String,
        nuevoUsername: String,
        nuevoBio: String,
        nuevoStatus: String,
        nuevoCharacter: String,
        nuevaImagenUrl: String? = null,
        nuevoBannerUrl: String? = null
    ) {
        try {
            supabase.from("users").update(
                {
                    set("username", nuevoUsername)
                    set("bio", nuevoBio)
                    set("status", nuevoStatus)
                    set("character_main", nuevoCharacter)
                    // Solo actualizamos las URLs si nos llega una nueva (no null)
                    if (nuevaImagenUrl != null) set("profile_image", nuevaImagenUrl)
                    if (nuevoBannerUrl != null) set("banner_image", nuevoBannerUrl)
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

    // --- FUNCIONES DE STORAGE (IMÁGENES) ---

    // 1. Subir Avatar (Foto de perfil redonda)
    // Se guardará como "profile_IDUSUARIO.jpg" para sobrescribir la anterior
    suspend fun subirAvatar(userId: String, byteArray: ByteArray): String {
        val bucket = supabase.storage.from("avatars")
        val fileName = "profile_$userId.jpg"
        bucket.upload(fileName, byteArray) { upsert = true }
        return bucket.publicUrl(fileName)
    }

    // 2. Subir Banner (Fondo rectangular)
    // Se guardará como "banner_IDUSUARIO.jpg"
    suspend fun subirBanner(userId: String, byteArray: ByteArray): String {
        val bucket = supabase.storage.from("avatars")
        val fileName = "banner_$userId.jpg"
        bucket.upload(fileName, byteArray) { upsert = true }
        return bucket.publicUrl(fileName)
    }
}