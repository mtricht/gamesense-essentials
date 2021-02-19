package dev.tricht.gamesense

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.jna.Platform
import dev.tricht.gamesense.com.steelseries.ApiClient
import dev.tricht.gamesense.com.steelseries.model.*
import dev.tricht.gamesense.events.DataFetcher
import dev.tricht.gamesense.events.EventProducer
import dev.tricht.gamesense.events.MacOSDataFetcher
import dev.tricht.gamesense.events.WindowsDataFetcher
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
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
var dataFetcher: DataFetcher? = null
var preferences: Preferences = Preferences.userNodeForPackage(Main::class.java)
var clockEnabled = preferences.get("clock", "true").toBoolean()
var clockIconEnabled = preferences.get("clockIcon", "true")!!.toBoolean()
var volumeEnabled = preferences.get("volume", "true").toBoolean()
var songInfoFlipEnabled = preferences.get("songInfoFlip", "false").toBoolean()

fun main() {
    SystemTray.setup()
    var connected = false
    while (!connected) {
        try {
            val address = Main.getGamesenseAddress()
            client = Main.buildClient(address)
            client?.ping()?.execute()
            connected = true
        } catch (e: Exception) {
            println("Failed to register app, steelseries engine probably not running? Retrying in 5 seconds")
            Thread.sleep(5000)
        }
    }
    Main.registerHandlers(client!!)
    dataFetcher = if (Platform.isWindows()) {
        WindowsDataFetcher()
    } else {
        MacOSDataFetcher()
    }
    Main.startTimer()
}

class Main {
    companion object {
        fun buildClient(address: String): ApiClient {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://$address")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build()
            return retrofit.create(ApiClient::class.java)
        }

        fun getGamesenseAddress(): String {
            val path = if (Platform.isWindows()) {
                "C:\\ProgramData\\SteelSeries\\SteelSeries Engine 3\\coreProps.json"
            } else {
                "/Library/Application Support/SteelSeries Engine 3/coreProps.json"
            }
            val json = File(path).readText(Charsets.UTF_8)
            val props: Props = mapper.readValue(json)
            return props.address
        }

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
            var response = client.addEvent(volumeHandler).execute()
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
                                iconId = if (clockIconEnabled) 15 else 0
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
            if(songInfoFlipEnabled) {
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
                                    23
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
                                    23
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
            timer.schedule(EventProducer(client!!, dataFetcher!!), 0, Tick.tickRateInMs())
        }
    }
}
