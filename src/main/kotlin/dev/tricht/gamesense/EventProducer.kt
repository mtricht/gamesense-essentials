package dev.tricht.gamesense

import dev.tricht.gamesense.model.Data
import dev.tricht.gamesense.model.Event
import net.bjoernpetersen.volctl.VolumeControl
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class EventProducer(val client: ApiClient): TimerTask() {

    private var waitTicks = 0
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val volumeControl = VolumeControl()
    private var currentVolume = volumeControl.volume

    override fun run() {
        if (currentVolume != volumeControl.volume) {
            waitTicks = 6
            currentVolume = volumeControl.volume
            client.sendEvent(
                Event(
                    GAME_NAME,
                    VOLUME_EVENT,
                    Data(
                        currentVolume
                    )
                )
            ).execute()
            return
        }
        if (waitTicks > 0) {
            --waitTicks
            return
        }
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
}