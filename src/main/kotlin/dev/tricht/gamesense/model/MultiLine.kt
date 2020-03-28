package dev.tricht.gamesense.model

import com.fasterxml.jackson.annotation.JsonProperty

data class MultiLine(
    val lines: List<HandlerData>,
    @JsonProperty("icon-id")
    val iconId: Int = 0
)