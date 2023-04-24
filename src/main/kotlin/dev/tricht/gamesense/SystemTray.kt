package dev.tricht.gamesense

import java.awt.*
import java.awt.SystemTray
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JOptionPane
import kotlin.system.exitProcess

class SystemTray {
    companion object {
        private var tickRateOptionPaneIsOpen = false

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
            listOf(
                title,
                createSettingMenuItem("Enable clock", "clock"),
                createSettingMenuItem("Enable clock icon", "clockIcon"),
                createSettingMenuItem("Display clock periodically", "clockPeriodically", false),
                createSettingMenuItem("Enable volume slider", "volume"),
                createSettingMenuItem("Enable song information", "songInfo"),
                createSettingMenuItem("Flip song title and artist", "songInfoFlip", false),
                tickRate,
                exit
            ).forEach(menu::add)
            val trayIconImage = ImageIO.read(Main::class.java.classLoader.getResource("icon.png"))
            val trayIconWidth = TrayIcon(trayIconImage).size.width
            val icon = TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH))
            icon.popupMenu = menu
            tray.add(icon)
        }

        private fun createSettingMenuItem(
            description: String,
            setting: String,
            default: Boolean = true
        ): CheckboxMenuItem {
            val settingMenuItem = CheckboxMenuItem(description)
            settingMenuItem.addItemListener {
                val menuItem = it.source as CheckboxMenuItem
                preferences.put(setting, menuItem.state.toString())
                if (setting == "clockIcon") {
                    Main.registerClockHandler(client!!)
                }
                if (setting == "songInfoFlip") {
                    Main.registerSongHandler(client!!)
                }
            }
            settingMenuItem.state = preferences.get(setting, default.toString()).toBoolean()
            return settingMenuItem
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
            } catch (_: Exception) {
            }
        }
    }
}
