package dev.tricht.gamesense

import dev.tricht.gamesense.model.Data
import dev.tricht.gamesense.model.Event
import dev.tricht.gamesense.model.Frame
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

class EventProducer(val client: ApiClient): TimerTask() {

    private var waitTicks = 0
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private var currentVolume = (SoundUtil.getMasterVolumeLevel() * 100).roundToInt()
    private var currentFullSongName = ""
    private var currentArtist = ""
    private var currentSong = ""

    override fun run() {
        val newVolume = (SoundUtil.getMasterVolumeLevel() * 100).roundToInt()
        if (currentVolume != newVolume) {
            currentVolume = newVolume
            sendVolumeEvent()
            return
        }
        if (waitTicks > 0) {
            --waitTicks
            return
        }
        val song = getSpotifySongName()
        if (song != "") {
            sendSpotifyEvent(song)
            return
        }
        sendClockEvent()
    }

    private fun sendClockEvent() {
        client.sendEvent(
            Event(
                GAME_NAME,
                CLOCK_EVENT,
                Data(
                    LocalDateTime.now().format(formatter)
                )
            )
        ).execute()
    }

    private fun sendSpotifyEvent(song: String) {
        if (song == currentFullSongName) {
            currentArtist = marquify(currentArtist)
            currentSong = marquify(currentSong)
        } else {
            currentFullSongName = song
            val songSplit = song.split(" - ")
            currentArtist = songSplit[0] + " "
            currentSong = songSplit.drop(1).joinToString(" - ") + " "
        }
        client.sendEvent(
            Event(
                GAME_NAME,
                SONG_EVENT,
                Data(
                    currentFullSongName + System.currentTimeMillis(), // This is unused, but Steelseries 'caches' the value. So we have to change it.
                    Frame(
                        currentSong,
                        currentArtist
                    )
                )
            )
        ).execute()
        waitTicks = 4
    }

    fun marquify(text: String): String {
        if (text.length <= 15) {
            return text
        }
        return text.substring(1) + text[0]
    }

    private fun sendVolumeEvent() {
        waitTicks = 20
        client.sendEvent(
            Event(
                GAME_NAME,
                VOLUME_EVENT,
                Data(
                    currentVolume
                )
            )
        ).execute()
    }

    fun getSpotifySongName(): String {
        val p = Runtime.getRuntime()
            .exec("""tasklist /fi "IMAGENAME eq spotify.exe" /fi "STATUS ne Not Responding" /v /nh /fo csv""")
        val `in` = Scanner(p.inputStream)

        var song = ""
        var line: String
        while (`in`.hasNext()) {
            line = `in`.nextLine()
            if (line.trim { it <= ' ' } != "") {
                val lines = line.split(",").toTypedArray()
                val potentialSongName = lines[lines.size - 1].replace("\"", "")
                if (potentialSongName == "N/A" || potentialSongName.startsWith("Spotify")
                    || potentialSongName.startsWith("INFO: ") || !potentialSongName.contains(" - ")) {
                    continue
                }
                song = potentialSongName
            }
        }
        return song
    }
}