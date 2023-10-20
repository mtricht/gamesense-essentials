package dev.tricht.gamesense.model

import dev.tricht.gamesense.Tick
import java.util.prefs.Preferences

var preferences: Preferences = Preferences.userNodeForPackage(ScrollingText::class.java)

data class ScrollingText(
    var _text: String,
    var maxDisplayLength: Int = if (preferences.get("songIcon", "true")!!.toBoolean()) 12 else 21
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
        if (_text.length > maxDisplayLength) {
            text = "$_text | "
            originalText = "$_text "
        }
    }

    private fun marquify(text: String): String {
        if (text.length <= maxDisplayLength) {
            return text
        }
        return text.substring(1) + text[0]
    }
}