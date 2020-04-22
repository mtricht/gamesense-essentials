package dev.tricht.gamesense.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Profile(
    val user: User
)
