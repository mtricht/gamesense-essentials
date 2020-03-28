package dev.tricht.gamesense

import dev.tricht.gamesense.model.Event
import dev.tricht.gamesense.model.EventRegistration
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiClient {
    @POST("/bind_game_event")
    fun addEvent(@Body body: EventRegistration): Call<Any>
    @POST("/game_event")
    fun sendEvent(@Body body: Event): Call<Any>
}