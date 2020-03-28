package dev.tricht.gamesense.model

data class EventRegistration(
    val game: String,
    val event: String,
    val handlers: List<Handler>
)