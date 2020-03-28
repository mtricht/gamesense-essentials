package dev.tricht.gamesense

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.tricht.gamesense.model.*
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.util.*

val mapper = jacksonObjectMapper()
const val GAME_NAME = "GAMESENSE_ESSENTIALS"
const val CLOCK_EVENT = "CLOCK"
const val VOLUME_EVENT = "VOLUME"
const val SONG_EVENT = "SONG"

fun main() {
    val address = getGamesenseAddress()
    val retrofit = Retrofit.Builder()
        .baseUrl("http://$address")
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .build()
    val client = retrofit.create(ApiClient::class.java)
    registerHandlers(client)
    val timer = Timer()
    timer.schedule(EventProducer(client), 0, 50)
}

fun getGamesenseAddress(): String {
    val json = File("C:\\ProgramData\\SteelSeries\\SteelSeries Engine 3\\coreProps.json").readText(Charsets.UTF_8)
    val props: Props = mapper.readValue(json)
    return props.address
}

fun registerHandlers(client: ApiClient) {
    val cloclHandler = EventRegistration(
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
    client.addEvent(cloclHandler).execute()
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
    client.addEvent(volumeHandler).execute()

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
    client.addEvent(songHandler).execute()
}
