package eu.tricht.gamesense.com.steelseries.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventRegistration(
    val game: String,
    val event: String,
    val handlers: List<Handler>,
    @JsonProperty("data_fields")
    val dataFields: List<DataField>? = null
)