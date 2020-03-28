package dev.tricht.gamesense

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.tricht.gamesense.model.*
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

val mapper = jacksonObjectMapper()
const val GAME_NAME = "GAMESENSE_ESSENTIALS"
const val CLOCK_EVENT = "CLOCK"

fun main() {
    val address = getGamesenseAddress()
    val retrofit = Retrofit.Builder()
        .baseUrl("http://$address")
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .build()
    val client = retrofit.create(ApiClient::class.java)
    registerHandlers(client)

    val timer = Timer()
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val task = object : TimerTask() {
        override fun run() {
            client.sendEvent(Event(
                GAME_NAME,
                CLOCK_EVENT,
                Data(
                    LocalDateTime.now().format(formatter)
                )
            )).execute()
        }
    }
    timer.schedule(task, 0, 1000)
}

fun getGamesenseAddress(): String {
    val json = File("C:\\ProgramData\\SteelSeries\\SteelSeries Engine 3\\coreProps.json").readText(Charsets.UTF_8)
    val props: Props = mapper.readValue(json)
    return props.address
}

fun registerHandlers(client: ApiClient) {
    val event = EventRegistration(
        GAME_NAME,
        CLOCK_EVENT,
        listOf(
            Handler(
                listOf(HandlerData(
                    iconId = 15
                ))
            )
        )
    )
    client.addEvent(event).execute()
}
