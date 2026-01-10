package com.example.tekkentournaments.repositories

import android.util.Log
import com.example.tekkentournaments.clases.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import supabase

object AuthRepository {

    suspend fun login(emailInput: String, passwordInput: String): Boolean {
        return try {
            supabase.auth.signInWith(Email) {
                email = emailInput
                password = passwordInput
            }
            true
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error login: ${e.message}")
            false
        }
    }

    suspend fun register(emailInput: String, passwordInput: String, usernameInput: String): Boolean {
        return try {
            supabase.auth.signUpWith(Email) {
                email = emailInput
                password = passwordInput
            }

            val userId = supabase.auth.currentUserOrNull()?.id ?: return false
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
            val sessionRestored = supabase.auth.loadFromStorage()
            Log.d("AuthRepo", "Sesión recuperada: $sessionRestored")
            sessionRestored
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error recuperando sesión: ${e.message}")
            false
        }
    }

    suspend fun logout() {
        supabase.auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }
}