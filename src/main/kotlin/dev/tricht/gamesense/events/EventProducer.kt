package dev.tricht.gamesense.events

import dev.tricht.gamesense.*
import dev.tricht.gamesense.com.steelseries.ApiClient
import dev.tricht.gamesense.com.steelseries.model.Data
import dev.tricht.gamesense.com.steelseries.model.Event
import dev.tricht.gamesense.com.steelseries.model.Frame
import dev.tricht.gamesense.model.SongInformation
import java.text.DateFormat
import java.util.*

class EventProducer(
    private val client: ApiClient,
    private val dataFetcher: DataFetcher
 ) : TimerTask() {
    private val dateFormat = DateFormat.getTimeInstance()
    private var volume: Int? = null
    private var waitTicks = 0
    private var currentSong: SongInformation? = null

    override fun run() {
        val oldVolume = this.volume
        this.volume = dataFetcher.getVolume()
        if (oldVolume != null && this.volume != oldVolume) {
            sendVolumeEvent()
            return
        }
        if (waitTicks > 0) {
            --waitTicks
            return
        }
        val potentialSong = dataFetcher.getCurrentSong()
        if (potentialSong != null && potentialSong != "") {
            if (currentSong == null || potentialSong != currentSong!!.fullSongName) {
                currentSong = SongInformation(potentialSong)
            }
            sendSongEvent()
            return
        }
        sendClockEvent()
    }

    private fun sendClockEvent() {
        if (!clockEnabled) {
            return
        }
        client.sendEvent(
            Event(
                GAME_NAME,
                CLOCK_EVENT,
                Data(
                    dateFormat.format(Date())
                )
            )
        ).execute()
        waitTicks = Tick.msToTicks(200)
    }

    private fun sendSongEvent() {
        val songName = currentSong!!.song()
        client.sendEvent(
            Event(
                GAME_NAME,
                SONG_EVENT,
                Data(
                    // This is unused, but Steelseries 'caches' the value. So we have to change it.
                    songName + System.currentTimeMillis(),
                    Frame(
                        songName,
                        currentSong!!.artist()
                    )
                )
            )
        ).execute()
        waitTicks = Tick.msToTicks(200)
    }

    private fun sendVolumeEvent() {
        if (this.volume == null || !volumeEnabled) {
            return
        }
        waitTicks = Tick.msToTicks(1000)
        client.sendEvent(
            Event(
                GAME_NAME,
                VOLUME_EVENT,
                Data(
                    this.volume!!
                )
            )
        ).execute()
    }
}