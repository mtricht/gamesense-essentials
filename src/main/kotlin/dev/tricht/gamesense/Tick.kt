package dev.tricht.gamesense

import com.sun.jna.Platform
import kotlin.math.ceil

class Tick {
    companion object {
        private val isWindows = Platform.isWindows()
        fun msToTicks(ms: Int): Int {
            return ceil(ms.toFloat() / tickRateInMs().toFloat()).toInt()
        }
        fun tickRateInMs(): Long {
            return if (isWindows) {
                50
            } else {
                200
            }
        }
    }
}