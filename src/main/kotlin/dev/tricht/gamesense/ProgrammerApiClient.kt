package dev.tricht.gamesense

import dev.tricht.gamesense.model.Profile
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ProgrammerApiClient {
    @GET("/api/profile/info")
    fun getProfile(@Query("name") name: String): Call<Profile>
}