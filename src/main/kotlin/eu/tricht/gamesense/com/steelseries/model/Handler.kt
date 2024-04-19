package eu.tricht.gamesense.com.steelseries.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonSerialize
data class Handler(
    val datas: List<Any>,
    @JsonProperty("device-type")
    val deviceType: String = "screened",
    val mode: String = "screen",
    val zone: String = "one"
)