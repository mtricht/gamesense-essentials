package eu.tricht.gamesense.events

interface DataFetcher {
    fun getVolume(): Int
    fun getCurrentSong(): String?
}