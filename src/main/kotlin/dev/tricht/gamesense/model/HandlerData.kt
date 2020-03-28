package dev.tricht.gamesense.model

import com.fasterxml.jackson.annotation.JsonProperty

data class HandlerData(
    @JsonProperty("has-text")
    val hasText: Boolean = true,
    val bold: Boolean = true,
    @JsonProperty("icon-id")
    val iconId: Int = 0
)