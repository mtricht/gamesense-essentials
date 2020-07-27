package dev.tricht.gamesense.events

import dev.tricht.gamesense.Tick

@OptIn(ExperimentalStdlibApi::class)
class MacOSDataFetcher : DataFetcher {
    private val volumeScript = arrayOf("osascript", "-e", "output volume of (get volume settings)")
    private val spotifyScript = arrayOf("osascript", "-e", "tell application \"System Events\" to get the name of the first window of (processes whose name is \"Spotify\")")
    private val runtime = Runtime.getRuntime()
    private var volumeWaitTicks = 0
    private var lastVolumeChange = System.currentTimeMillis()
    private var lastVolume = 0
    private var songWaitTicks = 0
    private var lastSong: String? = null

    override fun getVolume(): Int {
        if (volumeWaitTicks > 0) {
            --volumeWaitTicks
            if ((System.currentTimeMillis() - lastVolumeChange) > 1000) {
                return lastVolume
            }
        }
        return try {
            val process = runtime.exec(volumeScript)
            process.waitFor()
            volumeWaitTicks = Tick.msToTicks(1500)
            val volume = Integer.parseInt(process.inputStream.readAllBytes().decodeToString().trim())
            if (volume != lastVolume) {
                lastVolumeChange = System.currentTimeMillis()
            }
            lastVolume = volume
            volume
        } catch (e: Exception) {
            e.printStackTrace()
            lastVolume = 0
            0
        }
    }

    override fun getCurrentSong(): String? {
        if (songWaitTicks > 0) {
            --songWaitTicks
            return lastSong
        }
        val process = runtime.exec(spotifyScript)
        process.waitFor()
        songWaitTicks = Tick.msToTicks(1500)
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