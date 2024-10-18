package eu.tricht.gamesense.events

import eu.tricht.gamesense.*
import eu.tricht.gamesense.com.steelseries.ApiClientFactory
import eu.tricht.gamesense.com.steelseries.model.Data
import eu.tricht.gamesense.com.steelseries.model.Event
import eu.tricht.gamesense.com.steelseries.model.Frame
import eu.tricht.gamesense.model.SongInformation
import java.net.ConnectException
import java.text.DateFormat
import java.util.*
import kotlin.math.roundToInt

class EventProducer : TimerTask() {
    private val dataFetcher = DataFetcher()
    private var client = ApiClientFactory().createApiClient()
    private val dateFormat = DateFormat.getTimeInstance(0)
    private var volume: Int? = null
    private var waitTicks = 0
    private var displayClockPeriodically = 0
    private var currentSong: SongInformation? = null
    private var masterVolumeTimeout = 0

    override fun run() {
        try {
            handleTick()
        } catch (e: ConnectException) {
            client = ApiClientFactory().createApiClient()
        }
    }

    private fun handleTick() {
        val oldVolume = this.volume
        this.volume = getVolume()
        if (oldVolume != null && this.volume != oldVolume) {
            sendVolumeEvent()
            return
        }
        if (displayClockPeriodically > 0) {
            --displayClockPeriodically
        }
        if (waitTicks > 0) {
            --waitTicks
            return
        }
        if (preferences.get("clockPeriodically", "false").toBoolean() && displayClockPeriodically == 0) {
            sendClockEvent()
            displayClockPeriodically = Tick.msToTicks(10000)
            waitTicks = Tick.msToTicks(2000)
            return
        }
        val potentialSong = dataFetcher.getCurrentSong()
        if (preferences.get("songInfo", "true").toBoolean() && potentialSong != null && potentialSong != "") {
            if (currentSong == null || potentialSong != currentSong!!.fullSongName) {
                currentSong = SongInformation(potentialSong)
            }
            sendSongEvent()
            return
        }
        sendClockEvent()
    }

    private fun getVolume(): Int {
        if (masterVolumeTimeout == 25) {
            masterVolumeTimeout = 0
        }
        masterVolumeTimeout++
        return (SoundUtil.getMasterVolumeLevel() * 100).roundToInt()
    }

    private fun sendClockEvent() {
        if (!preferences.get("clock", "true").toBoolean()) {
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
        if (this.volume == null || !preferences.get("volume", "true").toBoolean()) {
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