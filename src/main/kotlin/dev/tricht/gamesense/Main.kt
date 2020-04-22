package dev.tricht.gamesense

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.tricht.gamesense.model.*
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.io.File
import java.util.*
import javax.swing.ImageIcon
import kotlin.system.exitProcess

val mapper = jacksonObjectMapper()
const val GAME_NAME = "MEINEKACKA"
const val TEXT_EVENT = "TEXT_EVENT"

fun main() {
    setupSystemtray()
    val address = getGamesenseAddress()
    val retrofit = Retrofit.Builder()
        .baseUrl("http://$address")
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .build()
    val client = retrofit.create(ApiClient::class.java)
    val retrofit2 = Retrofit.Builder()
        .baseUrl("https://pr0gramm.com")
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .build()
    val programmerClient = retrofit2.create(ProgrammerApiClient::class.java)
    registerHandler(client)
    val timer = Timer()
    timer.schedule(EventProducer(client, programmerClient), 0, 500)
}

private fun setupSystemtray() {
    if (!SystemTray.isSupported()) {
        ErrorUtil.showErrorDialogAndExit("System is not supported.");
        return
    }
    val tray = SystemTray.getSystemTray()
    val menu = PopupMenu("meineKACKA Gamesense")
    val title = MenuItem("meineKACKA Gamesense")
    title.isEnabled = false
    val exit = MenuItem("Exit")
    menu.add(title)
    menu.add(exit)
    exit.addActionListener { exitProcess(0) }
    val icon =
        TrayIcon(ImageIcon(EventProducer::class.java.classLoader.getResource("icon.png"), "meineKACKA Gamesense").image)
    icon.isImageAutoSize = true
    icon.popupMenu = menu
    tray.add(icon)
}

fun getGamesenseAddress(): String {
    val json = File("C:\\ProgramData\\SteelSeries\\SteelSeries Engine 3\\coreProps.json").readText(Charsets.UTF_8)
    val props: Props = mapper.readValue(json)
    return props.address
}

fun registerHandler(client: ApiClient) {
    val handler = EventRegistration(
        GAME_NAME,
        TEXT_EVENT,
        listOf(
            Handler(
                listOf(
                    HandlerData(
                        bold = true
                    )
                )
            )
        )
    )
    val response = client.addEvent(handler).execute()
    if (!response.isSuccessful) {
        println("Failed to add handler, error: " + response.errorBody()?.string())
        exitProcess(1)
    }
}
