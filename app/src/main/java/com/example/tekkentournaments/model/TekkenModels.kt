package com.example.tekkentournaments.model

data class CharacterData(
    val name: String,
    val moves: List<Move>
)

data class Move(
    val command: String,
    val startup: String,
    val onBlock: String
)