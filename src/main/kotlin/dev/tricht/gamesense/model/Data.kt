package dev.tricht.gamesense.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Data(
    val value: Any,
    val frame: Frame? = null
)