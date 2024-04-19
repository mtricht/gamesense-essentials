package eu.tricht.gamesense.com.steelseries.model

import com.fasterxml.jackson.annotation.JsonProperty

data class MultiLine(
    val lines: List<HandlerData>,
    @JsonProperty("icon-id")
    val iconId: Int = 0
)