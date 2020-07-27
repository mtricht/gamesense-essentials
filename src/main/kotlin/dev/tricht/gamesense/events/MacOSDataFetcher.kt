package dev.tricht.gamesense.events

@OptIn(ExperimentalStdlibApi::class)
class MacOSDataFetcher : DataFetcher {
    private val volumeScript = arrayOf("osascript", "-e", "output volume of (get volume settings)")
    private val spotifyScript = arrayOf("osascript", "-e", "tell application \"System Events\" to get the name of the first window of (processes whose name is \"Spotify\")")
    private val runtime = Runtime.getRuntime()
    private var waitTicks = 0
    private var lastSong: String? = null

    override fun getVolume(): Int {
        return try {
            val process = runtime.exec(volumeScript)
            process.waitFor()
            Integer.parseInt(process.inputStream.readAllBytes().decodeToString().trim())
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun getCurrentSong(): String? {
        if (waitTicks > 0) {
            --waitTicks
            return lastSong
        }
        val process = runtime.exec(spotifyScript)
        process.waitFor()
        waitTicks = 20
        if (process.exitValue() != 0) {
            println(process.inputStream.readAllBytes().decodeToString().trim())
            lastSong = null
            return null
        }
        val song = process.inputStream.readAllBytes().decodeToString().trim()
        if (song.contains(" - ")) {
            lastSong = song
            return song
        }
        lastSong = null
        return null
    }

}