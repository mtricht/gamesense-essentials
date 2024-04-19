package eu.tricht.gamesense.events

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
import eu.tricht.gamesense.SoundUtil
import eu.tricht.gamesense.Tick
import eu.tricht.gamesense.itunes.ITTrack
import java.io.File
import kotlin.math.roundToInt

class WindowsDataFetcher() : DataFetcher {

    private var iTunesIsRunning = false
    private var iTunes: ActiveXComponent? = null
    private var iTunesTimeout = 0
    private var masterVolumeTimeout = 0
    private val players = "(Spotify|MusicBee|AIMP|YouTube Music Desktop App).exe".toRegex()

    override fun getVolume(): Int {
        if (masterVolumeTimeout == 25) {
            masterVolumeTimeout = 0
        }
        masterVolumeTimeout++
        return (SoundUtil.getMasterVolumeLevel() * 100).roundToInt()
    }

    override fun getCurrentSong(): String? {
        return arrayOf(
            getPotentialSong(),
            getiTunesSongName(),
            getFoobarSongName()
        ).firstOrNull(String::isNotBlank)
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
            if (processPath.contains(players)) {
                val titleLength = User32.INSTANCE.GetWindowTextLength(hwnd) + 1
                val title = CharArray(titleLength)
                User32.INSTANCE.GetWindowText(hwnd, title, titleLength)
                val wText = Native.toString(title)
                if (wText.contains("MediaPlayer SMTC")) {
                    return@WNDENUMPROC true
                }
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
                --iTunesTimeout
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
            // to "really" close. Wait 1 second, before trying to connect again.
            // Else iTunes would just close and reopen immediately.
            iTunesTimeout = Tick.msToTicks(1000)
            iTunes = null
        }
        return song
    }

    private fun getFoobarSongName(): String {
        val nowPlayingFile = File(System.getenv("HOMEDRIVE") + System.getenv("HOMEPATH") + "\\foobar_np.txt")
        if (!nowPlayingFile.exists()) {
            return ""
        }
        val text = nowPlayingFile.readText(Charsets.UTF_8)
        if (!text.contains("playing: ")) {
            return ""
        }
        return text.replace("playing: ", "")
    }

}