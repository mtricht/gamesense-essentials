package dev.tricht.gamesense

import dev.tricht.gamesense.model.Profile
import retrofit2.Call
import retrofit2.http.GET

interface ProgrammerApiClient {
    @GET("/api/profile/info?name=meineKACKA")
    fun getProfile(): Call<Profile>
}