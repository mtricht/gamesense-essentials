package eu.tricht.gamesense

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import eu.tricht.gamesense.com.steelseries.ApiClient
import eu.tricht.gamesense.com.steelseries.ApiClientFactory
import eu.tricht.gamesense.com.steelseries.model.*
import eu.tricht.gamesense.events.EventProducer
import java.util.*
import java.util.prefs.Preferences
import kotlin.system.exitProcess

val mapper = jacksonObjectMapper()
const val GAME_NAME = "GAMESENSE_ESSENTIALS"
const val CLOCK_EVENT = "CLOCK"
const val VOLUME_EVENT = "VOLUME"
const val SONG_EVENT = "SONG"
var timer = Timer()
var client: ApiClient? = null
var preferences: Preferences = Preferences.userNodeForPackage(Main::class.java)

fun main() {
    SystemTray.setup()
    client = ApiClientFactory().createApiClient()
    Main.registerHandlers(client!!)
    Main.startTimer()
}

class Main {
    companion object {
        fun registerHandlers(client: ApiClient) {
            registerClockHandler(client)
            val volumeHandler = EventRegistration(
                GAME_NAME,
                VOLUME_EVENT,
                listOf(
                    Handler(
                        listOf(
                            MultiLine(
                                listOf(
                                    HandlerData(
                                        // Currently sets the arg to '()' instead of nothing
                                        arg = "",
                                        // So we fix that by adding some spaces on a prefix...
                                        prefix = "Volume" + " ".repeat(20)
                                    ),
                                    HandlerData(
                                        hasProgressBar = true,
                                        hasText = false
                                    )
                                ),
                                23
                            )
                        )
                    )
                )
            )
            val response = client.addEvent(volumeHandler).execute()
            if (!response.isSuccessful) {
                println("Failed to add volume handler, error: " + response.errorBody()?.string())
                exitProcess(1)
            }
            registerSongHandler(client)
        }

        fun registerClockHandler(client: ApiClient) {
            val clockHandler = EventRegistration(
                GAME_NAME,
                CLOCK_EVENT,
                listOf(
                    Handler(
                        listOf(
                            HandlerData(
                                iconId = if (preferences.get("clockIcon", "true")!!.toBoolean()) 15 else 0
                            )
                        )
                    )
                )
            )
            val response = client.addEvent(clockHandler).execute()
            if (!response.isSuccessful) {
                println("Failed to add clock icon handler, error: " + response.errorBody()?.string())
                exitProcess(1)
            }
        }

        fun registerSongHandler(client: ApiClient) {
            if (preferences.get("songInfoFlip", "false").toBoolean()) {
                val songHandler = EventRegistration(
                    GAME_NAME,
                    SONG_EVENT,
                    listOf(
                        Handler(
                            listOf(
                                MultiLine(
                                    listOf(
                                        HandlerData(
                                            contextFrameKey = "artist"
                                        ),
                                        HandlerData(
                                            contextFrameKey = "song"
                                        )
                                    ),
                                    if (preferences.get("songIcon", "true")!!.toBoolean()) 23 else 0
                                )
                            )
                        )
                    ),
                    listOf(
                        DataField(
                            "song",
                            "Song"
                        ),
                        DataField(
                            "artist",
                            "Artist"
                        )
                    )
                )
                val response = client.addEvent(songHandler).execute()
                if (!response.isSuccessful) {
                    println("Failed to add song handler, error: " + response.errorBody()?.string())
                    exitProcess(1)
                }
            } else {
                val songHandler = EventRegistration(
                    GAME_NAME,
                    SONG_EVENT,
                    listOf(
                        Handler(
                            listOf(
                                MultiLine(
                                    listOf(
                                        HandlerData(
                                            contextFrameKey = "song"
                                        ),
                                        HandlerData(
                                            contextFrameKey = "artist"
                                        )
                                    ),
                                    if (preferences.get("songIcon", "true")!!.toBoolean()) 23 else 0
                                )
                            )
                        )
                    ),
                    listOf(
                        DataField(
                            "song",
                            "Song"
                        ),
                        DataField(
                            "artist",
                            "Artist"
                        )
                    )
                )
                val response = client.addEvent(songHandler).execute()
                if (!response.isSuccessful) {
                    println("Failed to add song handler, error: " + response.errorBody()?.string())
                    exitProcess(1)
                }
            }
        }

        fun startTimer() {
            timer.schedule(EventProducer(), 0, Tick.tickRateInMs())
        }
    }
}
