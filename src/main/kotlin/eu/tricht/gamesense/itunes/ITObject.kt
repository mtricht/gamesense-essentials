package eu.tricht.gamesense.itunes

import com.jacob.com.Dispatch

open class ITObject(d: Dispatch?) {
    protected lateinit var `object`: Dispatch

    var name: String?
        get() = Dispatch.get(`object`, "Name").string
        set(name) {
            Dispatch.put(`object`, "Name", name)
        }

    init {
        if (d != null) {
            `object` = d
        }
    }
}