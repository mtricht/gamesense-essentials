package dev.tricht.gamesense

import com.sun.jna.Platform
import kotlin.math.ceil

class Tick {
    companion object {
        private val isWindows = Platform.isWindows()
        private var tickRate = preferences.get("tickRate", "")
        fun msToTicks(ms: Int): Int {
            return ceil(ms.toFloat() / tickRateInMs().toFloat()).toInt()
        }
        fun tickRateInMs(): Long {
            if (tickRate != "") {
                return tickRate.toLong()
            }
            return if (isWindows) {
                50
            } else {
                200
            }
        }
        fun refreshCache() {
            tickRate = preferences.get("tickRate", "")
        }
    }
}