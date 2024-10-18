package eu.tricht.gamesense.thchyoutube

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
data class Data(
    val title: String,
    val artist: String,
    val isPaused: Boolean
)