package dev.tricht.gamesense.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class HandlerData(
    @JsonProperty("has-text")
    val hasText: Boolean = true,
    val bold: Boolean = true,
    @JsonProperty("icon-id")
    val iconId: Int = 0,
    val arg: String? = null,
    val prefix: String = "",
    val suffix: String = "",
    @JsonProperty("has-progress-bar")
    val hasProgressBar: Boolean = false
)