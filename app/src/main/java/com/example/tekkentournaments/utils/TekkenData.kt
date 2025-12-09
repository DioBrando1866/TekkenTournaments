package com.example.tekkentournaments.utils

object TekkenData {

    // Lista de juegos soportados
    val gameVersions = listOf(
        "Tekken 8",
        "Tekken 7",
        "Tekken 6",
        "Tekken 5",
        "Tekken 4",
        "Tekken 3",
        "Tekken Tag 1",
        "Tekken Tag 2"
    )

    // Base de datos de personajes por juego
    private val rosters = mapOf(
        "Tekken 8" to listOf("Jin", "Kazuya", "Jun", "Reina", "Victor", "Azucena", "King", "Paul", "Law", "Xiaoyu", "Hwoarang", "Lars", "Alisa", "Claudio", "Shaheen", "Leroy", "Lili", "Asuka", "Bryan", "Yoshimitsu", "Raven", "Dragunov", "Feng", "Leo", "Steve", "Kuma", "Panda", "Zafina", "Lee", "Devil Jin", "Eddy", "Lidia"),

        "Tekken 7" to listOf("Akuma", "Katarina", "Lucky Chloe", "Josie", "Gigas", "Kazumi", "Heihachi", "Jin", "Kazuya", "Claudio", "Shaheen", "Leroy", "Fahkumram", "Lidia", "Kunimitsu", "Geese", "Noctis", "Negan"),

        "Tekken 3" to listOf("Jin", "Xiaoyu", "Hwoarang", "Eddy", "Forest Law", "Paul", "Lei", "King", "Nina", "Yoshimitsu", "Bryan", "Gun Jack", "Julia", "Ogre", "True Ogre", "Mokujin", "Gon", "Dr. Bosconovitch", "Heihachi"),

        "Tekken Tag 1" to listOf("Jin", "Kazuya", "Heihachi", "Jun", "Michelle", "Kunimitsu", "Angel", "Devil", "Unknown", "Ogre", "True Ogre", "Bruce", "Baek", "Roger", "Alex", "Prototype Jack")
    )

    // Función segura para obtener personajes
    fun getCharacters(game: String): List<String> {
        // Si no tenemos la lista específica, devolvemos una genérica o la de Tekken 8 por defecto
        val list = rosters[game] ?: rosters["Tekken 8"]!!
        return (list + "Random").sorted()
    }
}