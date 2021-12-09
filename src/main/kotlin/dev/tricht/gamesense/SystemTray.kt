package dev.tricht.gamesense

import java.awt.*
import java.awt.SystemTray
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JOptionPane
import kotlin.system.exitProcess


class SystemTray {
    companion object {
        var tickRateOptionPaneIsOpen = false

        fun setup() {
            if (!SystemTray.isSupported()) {
                ErrorUtil.showErrorDialogAndExit("System is not supported.");
                return
            }
            val tray = SystemTray.getSystemTray()
            val menu = PopupMenu("Gamesense Essentials")
            val title = MenuItem("Gamesense Essentials")
            title.isEnabled = false
            val exit = MenuItem("Exit")
            exit.addActionListener { exitProcess(0) }
            val tickRate = MenuItem("Change tick rate")
            tickRate.addActionListener { changeTickRate() }
            val clock = CheckboxMenuItem("Enable clock")
            clock.addItemListener {
                val menuItem = it.source as CheckboxMenuItem
                preferences.put("clock", menuItem.state.toString())
                clockEnabled = menuItem.state
            }
            clock.state = preferences.get("clock", "true").toBoolean()
            val clockIcon = CheckboxMenuItem("Enable clock icon")
            clockIcon.addItemListener {
                val menuItem = it.source as CheckboxMenuItem
                preferences.put("clockIcon", menuItem.state.toString())
                clockIconEnabled = menuItem.state
                Main.registerClockHandler(client!!)
            }
            clockIcon.state = preferences.get("clockIcon", "true")!!.toBoolean()
            val volume = CheckboxMenuItem("Enable volume slider")
            volume.addItemListener {
                val menuItem = it.source as CheckboxMenuItem
                preferences.put("volume", menuItem.state.toString())
                volumeEnabled = menuItem.state
            }
            val songInfoFlip = CheckboxMenuItem("Flip song title and artist")
            songInfoFlip.addItemListener {
                val menuItem = it.source as CheckboxMenuItem
                preferences.put("songInfoFlip", menuItem.state.toString())
                songInfoFlipEnabled = menuItem.state
                Main.registerSongHandler(client!!)
            }
            volume.state = preferences.get("volume", "true").toBoolean()
            menu.add(title)
            menu.add(volume)
            menu.add(clock)
            menu.add(clockIcon)
            menu.add(songInfoFlip)
            menu.add(tickRate)
            menu.add(exit)
            val trayIconImage = ImageIO.read(Main::class.java.classLoader.getResource("icon.png"))
            val trayIconWidth = TrayIcon(trayIconImage).size.width
            val icon = TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH))
            icon.popupMenu = menu
            tray.add(icon)
        }

        private fun changeTickRate() {
            if (tickRateOptionPaneIsOpen) {
                return
            }
            tickRateOptionPaneIsOpen = true
            val frame = JFrame()
            frame.isAlwaysOnTop = true
            val newTickRate = JOptionPane.showInputDialog(
                frame,
                "Tick rate in milliseconds. Lower means faster updates on the OLED screen but more CPU usage",
                Tick.tickRateInMs()
            )
            tickRateOptionPaneIsOpen = false
            if (newTickRate == null) {
                return
            }
            try {
                val newTickRateInt = newTickRate.trim().toInt()
                if (newTickRateInt <= 0) {
                    return
                }
                timer.cancel()
                timer.purge()
                preferences.put("tickRate", newTickRate.trim())
                Tick.refreshCache()
                timer = Timer()
                Main.startTimer()
            } catch (e: Exception) {
            }
        }
    }
}
