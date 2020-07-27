package dev.tricht.gamesense

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.jna.Platform
import dev.tricht.gamesense.com.steelseries.ApiClient
import dev.tricht.gamesense.com.steelseries.model.*
import dev.tricht.gamesense.events.EventProducer
import dev.tricht.gamesense.events.WindowsDataFetcher
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
const val GAME_NAME = "GAMESENSE_ESSENTIALS"
const val CLOCK_EVENT = "CLOCK"
const val VOLUME_EVENT = "VOLUME"
const val SONG_EVENT = "SONG"


fun main() {
    Main.setupSystemtray()
    var client: ApiClient? = null
    var connected = false
    while (!connected) {
        try {
            val address = Main.getGamesenseAddress()
            client = Main.buildClient(address)
            client.ping().execute()
            connected = true
        } catch (e: Exception) {
            println("Failed to register app, steelseries engine probably not running? Retrying in 5 seconds")
            Thread.sleep(5000)
        }
    }
    Main.registerHandlers(client!!)
    val timer = Timer()
    timer.schedule(EventProducer(client, WindowsDataFetcher()), 0, 50)
}

class Main {
    companion object {
        fun buildClient(address: String): ApiClient {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://$address")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build()
            return retrofit.create(ApiClient::class.java)
        }

        fun setupSystemtray() {
            if (!SystemTray.isSupported()) {
                ErrorUtil.showErrorDialogAndExit("System is not supported.");
                return
            }
            val tray = SystemTray.getSystemTray()
            val menu = PopupMenu("Gamesense Essentials")
            val title = MenuItem("Gamesense Essentials")
            title.isEnabled = false
            val exit = MenuItem("Exit")
            menu.add(title)
            menu.add(exit)
            exit.addActionListener { exitProcess(0) }
            val icon =
                TrayIcon(ImageIcon(Main::class.java.classLoader.getResource("icon.png"), "Gamesense Essentials").image)
            icon.isImageAutoSize = true
            icon.popupMenu = menu
            tray.add(icon)
        }

        fun getGamesenseAddress(): String {
            val path = if (Platform.isWindows()) {
                "C:\\ProgramData\\SteelSeries\\SteelSeries Engine 3\\coreProps.json"
            } else {
                "/Library/Application Support/SteelSeries Engine 3/coreProps.json"
            }
            val json = File(path).readText(Charsets.UTF_8)
            val props: Props = mapper.readValue(json)
            return props.address
        }

        fun registerHandlers(client: ApiClient) {
            val clockHandler = EventRegistration(
                GAME_NAME,
                CLOCK_EVENT,
                listOf(
                    Handler(
                        listOf(
                            HandlerData(
                                iconId = 15
                            )
                        )
                    )
                )
            )
            var response = client.addEvent(clockHandler).execute()
            if (!response.isSuccessful) {
                println("Failed to add clock handler, error: " + response.errorBody()?.string())
                exitProcess(1)
            }
            val volumeHandler = EventRegistration(
                GAME_NAME,
                VOLUME_EVENT,
                listOf(
                    Handler(
                        listOf(
                            MultiLine(
                                listOf(
                                    HandlerData(
                                        // Currently sets the arg to '()' instead of nothing
                                        arg = "",
                                        // So we fix that by adding some spaces on a prefix...
                                        prefix = "Volume" + " ".repeat(20)
                                    ),
                                    HandlerData(
                                        hasProgressBar = true,
                                        hasText = false
                                    )
                                ),
                                23
                            )
                        )
                    )
                )
            )
            response = client.addEvent(volumeHandler).execute()
            if (!response.isSuccessful) {
                println("Failed to add volume handler, error: " + response.errorBody()?.string())
                exitProcess(1)
            }
            val songHandler = EventRegistration(
                GAME_NAME,
                SONG_EVENT,
                listOf(
                    Handler(
                        listOf(
                            MultiLine(
                                listOf(
                                    HandlerData(
                                        contextFrameKey = "artist"
                                    ),
                                    HandlerData(
                                        contextFrameKey = "song"
                                    )
                                ),
                                23
                            )
                        )
                    )
                ),
                listOf(
                    DataField(
                        "artist",
                        "Artist"
                    ),
                    DataField(
                        "song",
                        "Song"
                    )
                )
            )
            response = client.addEvent(songHandler).execute()
            if (!response.isSuccessful) {
                println("Failed to add song handler, error: " + response.errorBody()?.string())
                exitProcess(1)
            }
        }
    }
}
