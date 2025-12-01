package com.example.tekkentournaments.repositories

import android.util.Log
import com.example.tekkentournaments.clases.Tournament
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
}