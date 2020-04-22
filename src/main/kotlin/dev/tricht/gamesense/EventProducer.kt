package dev.tricht.gamesense

import dev.tricht.gamesense.model.Data
import dev.tricht.gamesense.model.Event
import java.time.LocalDateTime
import java.util.*

class EventProducer(private val client: ApiClient, private val programmerClient: ProgrammerApiClient): TimerTask() {

    private var cacheTime: LocalDateTime? = null
    private var score = 0

    override fun run() {
        if (cacheTime == null || LocalDateTime.now().isAfter(cacheTime)) {
            println("Refreshing cache")
            val response = programmerClient.getProfile().execute()
            cacheTime = LocalDateTime.now().plusMinutes(2)
            if (response.body() != null) {
                score = response.body()!!.user.score
            }
        }
        sendScore()
    }

    private fun sendScore() {
        client.sendEvent(
            Event(
                GAME_NAME,
                TEXT_EVENT,
                Data(
                    ">_ $score"
                )
            )
        ).execute()
    }
}