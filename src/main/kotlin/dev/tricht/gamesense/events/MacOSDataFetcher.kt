package dev.tricht.gamesense.events

@OptIn(ExperimentalStdlibApi::class)
class MacOSDataFetcher : DataFetcher {
    private val volumeScript = arrayOf("osascript", "-e", "output volume of (get volume settings)")
    private val spotifyScript = arrayOf("osascript", "-e", "tell application \\\"System Events\\\" to get the name of the first window of (processes whose name is \\\"Spotify\\\")")
    private val runtime = Runtime.getRuntime()

    override fun getVolume(): Int {
        return try {
            val process = runtime.exec(volumeScript)
            Integer.parseInt(process.inputStream.readAllBytes().decodeToString().trim())
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun getCurrentSong(): String? {
        val process = runtime.exec(spotifyScript)
        return process.inputStream.readAllBytes().decodeToString().trim()
    }

}