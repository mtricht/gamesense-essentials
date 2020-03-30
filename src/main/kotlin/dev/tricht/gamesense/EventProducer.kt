package dev.tricht.gamesense

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Psapi
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import dev.tricht.gamesense.model.Data
import dev.tricht.gamesense.model.Event
import dev.tricht.gamesense.model.Frame
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

class EventProducer(val client: ApiClient): TimerTask() {

    private var waitTicks = 0
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private var currentVolume = (SoundUtil.getMasterVolumeLevel() * 100).roundToInt()
    private var currentFullSongName = ""
    private var currentArtist = ""
    private var currentSong = ""

    override fun run() {
        val newVolume = (SoundUtil.getMasterVolumeLevel() * 100).roundToInt()
        if (currentVolume != newVolume) {
            currentVolume = newVolume
            sendVolumeEvent()
            return
        }
        if (waitTicks > 0) {
            --waitTicks
            return
        }
        val song = getSpotifySongName()
        if (song != "") {
            sendSpotifyEvent(song)
            return
        }
        sendClockEvent()
    }

    private fun sendClockEvent() {
        client.sendEvent(
            Event(
                GAME_NAME,
                CLOCK_EVENT,
                Data(
                    LocalDateTime.now().format(formatter)
                )
            )
        ).execute()
    }

    private fun sendSpotifyEvent(song: String) {
        if (song == currentFullSongName) {
            currentArtist = marquify(currentArtist)
            currentSong = marquify(currentSong)
        } else {
            currentFullSongName = song
            val songSplit = song.split(" - ")
            currentArtist = songSplit[0] + " "
            currentSong = songSplit.drop(1).joinToString(" - ") + " "
        }
        client.sendEvent(
            Event(
                GAME_NAME,
                SONG_EVENT,
                Data(
                    currentFullSongName + System.currentTimeMillis(), // This is unused, but Steelseries 'caches' the value. So we have to change it.
                    Frame(
                        currentSong,
                        currentArtist
                    )
                )
            )
        ).execute()
        waitTicks = 4
    }

    fun marquify(text: String): String {
        if (text.length <= 15) {
            return text
        }
        return text.substring(1) + text[0]
    }

    private fun sendVolumeEvent() {
        waitTicks = 20
        client.sendEvent(
            Event(
                GAME_NAME,
                VOLUME_EVENT,
                Data(
                    currentVolume
                )
            )
        ).execute()
    }

    fun getSpotifySongName(): String {
        var song = ""
        val callback = WinUser.WNDENUMPROC { hwnd, _ ->
            val pointer = IntByReference()
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, pointer)
            val process = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_QUERY_INFORMATION or Kernel32.PROCESS_VM_READ, false, pointer.value
            )
            val baseNameBuffer = CharArray(1024 * 2)
            Psapi.INSTANCE.GetModuleFileNameExW(process, null, baseNameBuffer, 1024)
            val processPath: String = Native.toString(baseNameBuffer)
            if (processPath.contains("Spotify.exe")) {
                val titleLength = User32.INSTANCE.GetWindowTextLength(hwnd) + 1
                val title = CharArray(titleLength)
                User32.INSTANCE.GetWindowText(hwnd, title, titleLength)
                val wText = Native.toString(title)
                if (wText.contains(" - ")) {
                    song = wText
                }
            }
            true
        }
        User32.INSTANCE.EnumWindows(callback, Pointer.NULL)
        return song
    }
}