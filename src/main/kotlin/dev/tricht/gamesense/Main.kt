package dev.tricht.gamesense

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.tricht.gamesense.model.*
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.util.*
import kotlin.system.exitProcess

val mapper = jacksonObjectMapper()
const val GAME_NAME = "GAMESENSE_ESSENTIALS"
const val CLOCK_EVENT = "CLOCK"
const val VOLUME_EVENT = "VOLUME"
const val SONG_EVENT = "SONG"

fun main() {
    println("Starting gamesense-essentials...")
    val address = getGamesenseAddress()
    println("Address found: $address")
    val retrofit = Retrofit.Builder()
        .baseUrl("http://$address")
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .build()
    val client = retrofit.create(ApiClient::class.java)
    println("Adding handlers...")
    registerHandlers(client)
    println("Startup successful!\nLeave this command prompt open and see your OLED screen.")
    val timer = Timer()
    timer.schedule(EventProducer(client), 0, 50)
}

fun getGamesenseAddress(): String {
    val json = File("C:\\ProgramData\\SteelSeries\\SteelSeries Engine 3\\coreProps.json").readText(Charsets.UTF_8)
    val props: Props = mapper.readValue(json)
    return props.address
}

fun registerHandlers(client: ApiClient) {
    val clockHandler = EventRegistration(
        GAME_NAME,
        CLOCK_EVENT,
        listOf(
            Handler(
                listOf(
                    HandlerData(
                        iconId = 15
                    )
                )
            )
        )
    )
    var response = client.addEvent(clockHandler).execute()
    if (!response.isSuccessful) {
        println("Failed to add clock handler, error: " + response.errorBody()?.string())
        exitProcess(1)
    }
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
    response = client.addEvent(volumeHandler).execute()
    if (!response.isSuccessful) {
        println("Failed to add volume handler, error: " + response.errorBody()?.string())
        exitProcess(1)
    }
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
        )
    )
    response = client.addEvent(songHandler).execute()
    if (!response.isSuccessful) {
        println("Failed to add song handler, error: " + response.errorBody()?.string())
        exitProcess(1)
    }
}
