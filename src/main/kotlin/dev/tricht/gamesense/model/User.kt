package dev.tricht.gamesense.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val score: Int
)