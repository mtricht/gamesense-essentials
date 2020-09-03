package dev.tricht.gamesense

import java.awt.*
import java.awt.SystemTray
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.util.*
import javax.swing.ImageIcon
import javax.swing.JOptionPane
import kotlin.system.exitProcess

class SystemTray {
    companion object {
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
            val volume = CheckboxMenuItem("Enable volume slider")
            volume.addItemListener {
                val menuItem = it.source as CheckboxMenuItem
                preferences.put("volume", menuItem.state.toString())
                volumeEnabled = menuItem.state
            }
            volume.state = preferences.get("volume", "true").toBoolean()
            menu.add(title)
            menu.add(volume)
            menu.add(clock)
            menu.add(tickRate)
            menu.add(exit)
            val icon =
                TrayIcon(ImageIcon(Main::class.java.classLoader.getResource("icon.png"), "Gamesense Essentials").image)
            icon.isImageAutoSize = true
            icon.popupMenu = menu
            tray.add(icon)
        }

        private fun changeTickRate() {
            val newTickRate = JOptionPane.showInputDialog(
                "Tick rate in milliseconds. Lower means faster updates on the OLED screen but more CPU usage",
                Tick.tickRateInMs()
            )
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