package eu.tricht.gamesense

import javax.swing.JOptionPane
import kotlin.system.exitProcess

object ErrorUtil {
    fun showErrorDialogAndExit(message: String?) {
        JOptionPane.showMessageDialog(null, message, "gamesense essentials", JOptionPane.ERROR_MESSAGE)
        exitProcess(1)
    }
}