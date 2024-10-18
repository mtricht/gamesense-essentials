package eu.tricht.gamesense.thchyoutube

import retrofit2.Call
import retrofit2.http.GET

interface ApiClient {
    @GET("/api/v1/song-info")
    fun getSongInfo(): Call<Data>

    @GET("/")
    fun ping(): Call<Any>
}