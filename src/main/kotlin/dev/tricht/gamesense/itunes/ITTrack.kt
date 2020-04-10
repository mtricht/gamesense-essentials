package dev.tricht.gamesense.itunes

import com.jacob.com.Dispatch
import dev.tricht.gamesense.itunes.ITObject

class ITTrack(d: Dispatch?) : ITObject(d) {
    var artist: String?
        get() = Dispatch.get(`object`, "Artist").string
        set(artist) {
            Dispatch.put(`object`, "Artist", artist)
        }
}
