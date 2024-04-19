package eu.tricht.gamesense

import kotlin.math.ceil

class Tick {
    companion object {
        private var tickRate = preferences.get("tickRate", "")
        fun msToTicks(ms: Int): Int {
            return ceil(ms.toFloat() / tickRateInMs().toFloat()).toInt()
        }
        fun tickRateInMs(): Long {
            if (tickRate != "") {
                return tickRate.toLong()
            }
            return 50
        }
        fun refreshCache() {
            tickRate = preferences.get("tickRate", "")
        }
    }
}