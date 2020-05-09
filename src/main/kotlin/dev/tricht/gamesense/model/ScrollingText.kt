package dev.tricht.gamesense.model

data class ScrollingText(
    var _text: String
) {

    companion object {
        const val MAX_DISPLAY_TEXT_LENGTH = 15
    }

    var text = _text
    get() {
        if (ticksToWait > 0) {
            ticksToWait--
            allowedToPause = false
            return field
        }
        if (field == originalText && allowedToPause) {
            ticksToWait = 4
            return field
        }
        allowedToPause = true
        field = marquify(field)
        return field
    }

    private var allowedToPause = true
    private var ticksToWait = 4
    private var originalText = _text

    init {
        if (_text.length >= MAX_DISPLAY_TEXT_LENGTH) {
            text = "$_text "
            originalText = "$_text "
        }
    }

    private fun marquify(text: String): String {
        if (text.length <= MAX_DISPLAY_TEXT_LENGTH) {
            return text
        }
        return text.substring(1) + text[0]
    }
}