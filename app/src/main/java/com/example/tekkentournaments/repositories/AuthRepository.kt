package com.example.tekkentournaments.repositories

import android.util.Log
import com.example.tekkentournaments.clases.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import supabase

object AuthRepository {

    // 1. Iniciar Sesión
    suspend fun login(emailInput: String, passwordInput: String): Boolean {
        return try {
            supabase.auth.signInWith(Email) {
                email = emailInput
                password = passwordInput
            }
            true // Login exitoso
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error login: ${e.message}")
            false
        }
    }

    // 2. Registrarse (Auth + Crear fila en tabla 'users')
    suspend fun register(emailInput: String, passwordInput: String, usernameInput: String): Boolean {
        return try {
            // A. Crear usuario en el sistema de Auth de Supabase
            supabase.auth.signUpWith(Email) {
                email = emailInput
                password = passwordInput
            }

            // B. Obtener el ID del usuario recién creado
            val userId = supabase.auth.currentUserOrNull()?.id ?: return false

            // C. Insertar los datos extra en tu tabla pública 'users'
            // Usamos el mismo ID que Auth generó para vincularlos
            val newUser = User(
                id = userId,
                username = usernameInput,
                bio = "Nuevo luchador",
                status = "Online"
            )

            supabase.from("users").insert(newUser)

            true
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error registro: ${e.message}")
            false
        }
    }

    suspend fun recuperarSesion(): Boolean {
        return try {
            // Esto busca en las SharedPreferences (memoria segura) de Android
            // Si encuentra un token válido (o lo puede refrescar), devuelve true.
            val sessionRestored = supabase.auth.loadFromStorage()
            Log.d("AuthRepo", "Sesión recuperada: $sessionRestored")
            sessionRestored
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error recuperando sesión: ${e.message}")
            false
        }
    }

    // 3. Cerrar Sesión
    suspend fun logout() {
        supabase.auth.signOut()
    }

    // 4. Comprobar si ya está logueado
    fun isUserLoggedIn(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }
}