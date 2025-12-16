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

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadMatches(tournamentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val realMatches = TournamentRepository.obtenerPartidosDelTorneo(tournamentId)
            _matches.value = realMatches
            _isLoading.value = false
        }
    }

    fun reportMatchResult(matchId: String, scoreP1: Int, scoreP2: Int) {
        viewModelScope.launch {
            val currentMatch = _matches.value.find { it.id == matchId } ?: return@launch

            val winsNeeded = (currentMatch.maxScore / 2) + 1

            var winnerId: String? = null
            if (scoreP1 >= winsNeeded) {
                winnerId = currentMatch.player1Id
            } else if (scoreP2 >= winsNeeded) {
                winnerId = currentMatch.player2Id
            }

            TournamentRepository.actualizarPartido(matchId, scoreP1, scoreP2, winnerId)

            loadMatches(currentMatch.tournamentId)
        }
    }
}