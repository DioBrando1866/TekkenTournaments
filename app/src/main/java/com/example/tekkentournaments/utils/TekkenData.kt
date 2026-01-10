package com.example.tekkentournaments.utils

object TekkenData {

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

    private val rosters = mapOf(
        "Tekken 8" to listOf("Jin", "Kazuya", "Jun", "Reina", "Victor", "Azucena", "King", "Paul", "Law", "Xiaoyu", "Hwoarang", "Lars", "Alisa", "Claudio", "Shaheen", "Leroy", "Lili", "Asuka", "Bryan", "Yoshimitsu", "Raven", "Dragunov", "Feng", "Leo", "Steve", "Kuma", "Panda", "Zafina", "Lee", "Devil Jin", "Eddy", "Lidia"),

        "Tekken 7" to listOf("Akuma", "Katarina", "Lucky Chloe", "Josie", "Gigas", "Kazumi", "Heihachi", "Jin", "Kazuya", "Claudio", "Shaheen", "Leroy", "Fahkumram", "Lidia", "Kunimitsu", "Geese", "Noctis", "Negan"),

        "Tekken 3" to listOf("Jin", "Xiaoyu", "Hwoarang", "Eddy", "Forest Law", "Paul", "Lei", "King", "Nina", "Yoshimitsu", "Bryan", "Gun Jack", "Julia", "Ogre", "True Ogre", "Mokujin", "Gon", "Dr. Bosconovitch", "Heihachi"),

        "Tekken Tag 1" to listOf("Jin", "Kazuya", "Heihachi", "Jun", "Michelle", "Kunimitsu", "Angel", "Devil", "Unknown", "Ogre", "True Ogre", "Bruce", "Baek", "Roger", "Alex", "Prototype Jack")
    )

    fun getCharacters(game: String): List<String> {
        val list = rosters[game] ?: rosters["Tekken 8"]!!
        return (list + "Random").sorted()
    }


    fun getCharacterImageUrl(charName: String): String {
        val key = charName.lowercase().replace(" ", "").replace("-", "")

        return when {
            key.contains("jin") -> "https://liquipedia.net/commons/images/thumb/3/36/T8_Jin_Render.png/437px-T8_Jin_Render.png"
            key.contains("kazuya") -> "https://liquipedia.net/commons/images/thumb/f/f8/T8_Kazuya_Render.png/462px-T8_Kazuya_Render.png"
            key.contains("king") -> "https://liquipedia.net/commons/images/thumb/9/96/T8_King_Render.png/475px-T8_King_Render.png"
            key.contains("jun") -> "https://liquipedia.net/commons/images/thumb/d/df/T8_Jun_Render.png/347px-T8_Jun_Render.png"
            key.contains("paul") -> "https://liquipedia.net/commons/images/thumb/4/42/T8_Paul_Render.png/407px-T8_Paul_Render.png"
            key.contains("law") -> "https://liquipedia.net/commons/images/thumb/e/e5/T8_Marshall_Law_Render.png/480px-T8_Marshall_Law_Render.png"
            key.contains("xiaoyu") -> "https://liquipedia.net/commons/images/thumb/9/90/T8_Ling_Xiaoyu_Render.png/403px-T8_Ling_Xiaoyu_Render.png"
            key.contains("jack") -> "https://liquipedia.net/commons/images/thumb/a/a2/T8_Jack-8_Render.png/600px-T8_Jack-8_Render.png"
            key.contains("nina") -> "https://liquipedia.net/commons/images/thumb/e/e0/T8_Nina_Render.png/394px-T8_Nina_Render.png"
            key.contains("asuka") -> "https://liquipedia.net/commons/images/thumb/5/52/T8_Asuka_Render.png/462px-T8_Asuka_Render.png"
            key.contains("leroy") -> "https://liquipedia.net/commons/images/thumb/8/8c/T8_Leroy_Render.png/410px-T8_Leroy_Render.png"
            key.contains("lili") -> "https://liquipedia.net/commons/images/thumb/2/29/T8_Lili_Render.png/402px-T8_Lili_Render.png"
            key.contains("hwoarang") -> "https://liquipedia.net/commons/images/thumb/0/03/T8_Hwoarang_Render.png/461px-T8_Hwoarang_Render.png"
            key.contains("bryan") -> "https://liquipedia.net/commons/images/thumb/1/1e/T8_Bryan_Render.png/456px-T8_Bryan_Render.png"
            key.contains("claudio") -> "https://liquipedia.net/commons/images/thumb/c/ca/T8_Claudio_Render.png/461px-T8_Claudio_Render.png"
            key.contains("azucena") -> "https://liquipedia.net/commons/images/thumb/b/b5/T8_Azucena_Render.png/417px-T8_Azucena_Render.png"
            key.contains("raven") -> "https://liquipedia.net/commons/images/thumb/3/30/T8_Raven_Render.png/501px-T8_Raven_Render.png"
            key.contains("yoshimitsu") -> "https://liquipedia.net/commons/images/thumb/7/7b/T8_Yoshimitsu_Render.png/571px-T8_Yoshimitsu_Render.png"
            key.contains("steve") -> "https://liquipedia.net/commons/images/thumb/8/89/T8_Steve_Render.png/438px-T8_Steve_Render.png"
            key.contains("dragunov") -> "https://liquipedia.net/commons/images/thumb/d/d4/T8_Dragunov_Render.png/419px-T8_Dragunov_Render.png"
            key.contains("leo") -> "https://liquipedia.net/commons/images/thumb/8/86/T8_Leo_Render.png/436px-T8_Leo_Render.png"
            key.contains("shaheen") -> "https://liquipedia.net/commons/images/thumb/a/a3/T8_Shaheen_Render.png/417px-T8_Shaheen_Render.png"
            key.contains("kuma") -> "https://liquipedia.net/commons/images/thumb/6/6a/T8_Kuma_Render.png/526px-T8_Kuma_Render.png"
            key.contains("panda") -> "https://liquipedia.net/commons/images/thumb/e/e2/T8_Panda_Render.png/452px-T8_Panda_Render.png"
            key.contains("zafina") -> "https://liquipedia.net/commons/images/thumb/a/a2/T8_Zafina_Render.png/407px-T8_Zafina_Render.png"
            key.contains("lee") -> "https://liquipedia.net/commons/images/thumb/e/ee/T8_Lee_Render.png/457px-T8_Lee_Render.png"
            key.contains("alisa") -> "https://liquipedia.net/commons/images/thumb/c/c8/T8_Alisa_Render.png/530px-T8_Alisa_Render.png"
            key.contains("devil") -> "https://liquipedia.net/commons/images/thumb/8/81/T8_Devil_Jin_Render.png/562px-T8_Devil_Jin_Render.png"
            key.contains("feng") -> "https://liquipedia.net/commons/images/thumb/0/07/T8_Feng_Render.png/470px-T8_Feng_Render.png"
            key.contains("victor") -> "https://liquipedia.net/commons/images/thumb/7/77/T8_Victor_Render.png/410px-T8_Victor_Render.png"
            key.contains("reina") -> "https://liquipedia.net/commons/images/thumb/8/86/T8_Reina_Render.png/382px-T8_Reina_Render.png"
            key.contains("eddy") -> "https://liquipedia.net/commons/images/thumb/e/e9/T8_Eddy_Render.png/450px-T8_Eddy_Render.png"

            key.contains("random") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Pok%C3%A9_Ball_icon.svg/1200px-Pok%C3%A9_Ball_icon.svg.png"

            else -> "https://ui-avatars.com/api/?name=$charName&background=random&color=fff&size=256&bold=true"
        }
    }
}