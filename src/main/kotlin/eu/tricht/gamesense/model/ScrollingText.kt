package eu.tricht.gamesense.model

import eu.tricht.gamesense.Tick
import eu.tricht.gamesense.preferences

data class ScrollingText(
    var _text: String
) {

    var text = _text
    get() {
        if (ticksToWait > 0) {
            ticksToWait--
            allowedToPause = false
            return field
        }
        if (field == originalText && allowedToPause) {
            ticksToWait = Tick.msToTicks(200)
            return field
        }
        allowedToPause = true
        field = marquify(field)
        return field
    }

    private var allowedToPause = true
    private var ticksToWait = Tick.msToTicks(200)
    private var originalText = _text

    init {
        if (_text.length > getMaxDisplayLength()) {
            text = "$_text " + getSongSeparator()
            originalText = "$_text "
        }
    }

    private fun marquify(text: String): String {
        if (text.length <= getMaxDisplayLength()) {
            return text
        }
        return text.substring(1) + text[0]
    }

    private fun getMaxDisplayLength(): Int {
        return if (preferences.get("songIcon", "true")!!.toBoolean()) 12 else 21
    }

    private fun getSongSeparator(): String {
        return if (preferences.get("songSeparator", "false")!!.toBoolean()) "| " else ""
    }
}