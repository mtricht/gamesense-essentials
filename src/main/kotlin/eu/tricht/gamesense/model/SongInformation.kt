package eu.tricht.gamesense.model

data class SongInformation(
    val fullSongName: String
) {
    private val artistText = ScrollingText(fullSongName.split(" - ")[0])
    private val songText = ScrollingText(fullSongName.split(" - ")[1])
    fun artist() = artistText.text
    fun song() = songText.text
}