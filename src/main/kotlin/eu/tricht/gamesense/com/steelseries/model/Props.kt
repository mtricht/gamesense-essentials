package eu.tricht.gamesense.com.steelseries.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Props(val address: String)