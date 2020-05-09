package dev.tricht.gamesense

import com.jacob.activeX.ActiveXComponent
import com.jacob.com.ComFailException
import com.jacob.com.ComThread
import com.jacob.com.Dispatch
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Psapi
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import dev.tricht.gamesense.itunes.ITTrack
import dev.tricht.gamesense.model.Data
import dev.tricht.gamesense.model.Event
import dev.tricht.gamesense.model.Frame
import java.text.DateFormat
import java.util.*
import kotlin.math.roundToInt

class EventProducer(private val client: ApiClient): TimerTask() {

    private var waitTicks = 0
    private val dateFormat = DateFormat.getTimeInstance()
    private var currentVolume = (SoundUtil.getMasterVolumeLevel() * 100).roundToInt()
    private var currentFullSongName = ""
    private var currentArtist = ""
    private var currentSong = ""
    private var iTunesIsRunning = false
    private var iTunes: ActiveXComponent? = null
    private var iTunesTimeout = 0

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
        val potentialSong = getPotentialSong()
        if (potentialSong != "") {
            sendSongEvent(potentialSong)
            return
        }
        val iTunesSong = getiTunesSongName()
        if (iTunesSong != "") {
            sendSongEvent(iTunesSong)
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
                    dateFormat.format(Date())
                )
            )
        ).execute()
    }

    private fun sendSongEvent(song: String) {
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

    private fun marquify(text: String): String {
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

    private fun getPotentialSong(): String {
        iTunesIsRunning = false
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
            if (processPath.endsWith("Spotify.exe") || processPath.endsWith("MusicBee.exe")) {
                val titleLength = User32.INSTANCE.GetWindowTextLength(hwnd) + 1
                val title = CharArray(titleLength)
                User32.INSTANCE.GetWindowText(hwnd, title, titleLength)
                val wText = Native.toString(title)
                if (wText.contains(" - ")) {
                    song = wText.replace(" - MusicBee", "")
                }
            }
            if (processPath.endsWith("iTunes.exe")) {
                iTunesIsRunning = true
            }
            Kernel32.INSTANCE.CloseHandle(process)
            true
        }
        User32.INSTANCE.EnumWindows(callback, Pointer.NULL)
        return song
    }

    private fun getiTunesSongName(): String {
        if (!iTunesIsRunning) {
            return ""
        }
        if (iTunes == null) {
            if (iTunesTimeout != 0) {
                iTunesTimeout -= 1
                return ""
            }
            ComThread.InitMTA(true);
            iTunes = ActiveXComponent("iTunes.Application")
        }
        var song = ""
        try {
            if (Dispatch.get(iTunes, "PlayerState").int == 1) {
                val item = iTunes?.getProperty("CurrentTrack")?.toDispatch()
                val track = ITTrack(item)
                song = track.artist + " - " + track.name
                item?.safeRelease()
            }
        } catch (ec: ComFailException) {
            // This probably means iTunes was closed. However, it takes some time for iTunes
            // to "really" close. Wait 20 ticks, before trying to connect again.
            // Else iTunes would just close and reopen immediately.
            iTunesTimeout = 20
            iTunes = null
        }
        return song
    }
}