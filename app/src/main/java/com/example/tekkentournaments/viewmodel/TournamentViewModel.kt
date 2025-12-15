package com.example.tekkentournaments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tekkentournaments.clases.Match
import com.example.tekkentournaments.repositories.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TournamentViewModel : ViewModel() {

    // Usamos la clase Match de SUPABASE, no la de Demo
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Cargar partidos reales desde Supabase
    fun loadMatches(tournamentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Llamamos al repositorio que ya configuramos
            val realMatches = TournamentRepository.obtenerPartidosDelTorneo(tournamentId)
            _matches.value = realMatches
            _isLoading.value = false
        }
    }

    // Reportar resultado a Supabase
    fun reportMatchResult(matchId: String, scoreP1: Int, scoreP2: Int) {
        viewModelScope.launch {
            // 1. Buscamos el match actual para saber los IDs de los jugadores
            val currentMatch = _matches.value.find { it.id == matchId } ?: return@launch

            // 2. Calculamos quién gana según el maxScore del match
            // (Si es FT3, gana quien llegue a 3. Si es BO3 (max 3 partidas), gana quien llegue a 2)
            val winsNeeded = (currentMatch.maxScore / 2) + 1

            var winnerId: String? = null
            if (scoreP1 >= winsNeeded) {
                winnerId = currentMatch.player1Id
            } else if (scoreP2 >= winsNeeded) {
                winnerId = currentMatch.player2Id
            }

            // 3. Guardamos en Supabase
            TournamentRepository.actualizarPartido(matchId, scoreP1, scoreP2, winnerId)

            // 4. Recargamos la lista para actualizar la UI
            // NOTA: Aquí en el futuro podrías añadir la lógica de "Crear siguiente partido"
            // si el torneo avanza de ronda.
            loadMatches(currentMatch.tournamentId)
        }
    }
}