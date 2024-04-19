package eu.tricht.gamesense.itunes

import com.jacob.com.Dispatch

class ITTrack(d: Dispatch?) : ITObject(d) {
    var artist: String?
        get() = Dispatch.get(`object`, "Artist").string
        set(artist) {
            Dispatch.put(`object`, "Artist", artist)
        }
}
