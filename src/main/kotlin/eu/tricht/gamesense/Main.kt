package eu.tricht.gamesense

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import eu.tricht.gamesense.com.steelseries.ApiClient
import eu.tricht.gamesense.com.steelseries.ApiClientFactory
import eu.tricht.gamesense.com.steelseries.model.*
import eu.tricht.gamesense.events.EventProducer
import java.util.*
import java.util.prefs.Preferences
import kotlin.system.exitProcess
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener

val mapper = jacksonObjectMapper()
const val GAME_NAME = "GAMESENSE_ESSENTIALS"
const val CLOCK_EVENT = "CLOCK"
const val VOLUME_EVENT = "VOLUME"
const val SONG_EVENT = "SONG"
const val CALCULATOR_EVENT = "CALCULATOR"
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
        var eventProducer: EventProducer? = null

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
            registerCalculatorHandler(client)
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
                                            contextFrameKey = "text2"
                                        ),
                                        HandlerData(
                                            contextFrameKey = "text1"
                                        )
                                    ),
                                    if (preferences.get("songIcon", "true")!!.toBoolean()) 23 else 0
                                )
                            )
                        )
                    ),
                    listOf(
                        DataField(
                            "text1",
                            "Song"
                        ),
                        DataField(
                            "text2",
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
                                            contextFrameKey = "text1"
                                        ),
                                        HandlerData(
                                            contextFrameKey = "text2"
                                        )
                                    ),
                                    if (preferences.get("songIcon", "true")!!.toBoolean()) 23 else 0
                                )
                            )
                        )
                    ),
                    listOf(
                        DataField(
                            "text1",
                            "Song"
                        ),
                        DataField(
                            "text2",
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

        fun registerCalculatorHandler(client: ApiClient) {
            val calculatorHandler = EventRegistration(
                GAME_NAME,
                CALCULATOR_EVENT,
                listOf(
                    Handler(
                        listOf(
                            MultiLine(
                                listOf(
                                    HandlerData(
                                        contextFrameKey = "text1"
                                    ),
                                    HandlerData(
                                        contextFrameKey = "text2"
                                    )
                                )
                            )
                        )
                    )
                ),
                listOf(
                    DataField(
                        "text1",
                        "Input"
                    ),
                    DataField(
                        "text2",
                        "Answer"
                    )
                )
            )
            val response = client.addEvent(calculatorHandler).execute()
            if (!response.isSuccessful) {
                println("Failed to add calculator handler, error: " + response.errorBody()?.string())
                exitProcess(1)
            }
        }

        fun startTimer() {
            eventProducer = EventProducer()
            timer.schedule(eventProducer, 0, Tick.tickRateInMs())
        }
    }
}
