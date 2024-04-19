package eu.tricht.gamesense.com.steelseries.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class HandlerData(
    @JsonProperty("has-text")
    val hasText: Boolean = true,
    val bold: Boolean = false,
    @JsonProperty("icon-id")
    val iconId: Int = 0,
    val arg: String? = null,
    val prefix: String = "",
    val suffix: String = "",
    @JsonProperty("has-progress-bar")
    val hasProgressBar: Boolean = false,
    @JsonProperty("context-frame-key")
    val contextFrameKey: String? = null,
    val wrap: Int = 0
)