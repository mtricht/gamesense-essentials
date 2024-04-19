package eu.tricht.gamesense.com.steelseries

import com.fasterxml.jackson.module.kotlin.readValue
import eu.tricht.gamesense.com.steelseries.model.Props
import eu.tricht.gamesense.mapper
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File

class ApiClientFactory {
    fun createApiClient(): ApiClient {
        while (true) {
            try {
                val address = getAddress()
                val client = buildClient(address)
                client.ping().execute()
                return client
            } catch (e: Exception) {
                println("Failed to register app, steelseries engine probably not running? Retrying in 5 seconds")
                Thread.sleep(5000)
            }
        }
    }

    private fun getAddress(): String {
        val path = System.getenv("PROGRAMDATA") + "\\SteelSeries\\SteelSeries Engine 3\\coreProps.json"
        val json = File(path).readText(Charsets.UTF_8)
        val props: Props = mapper.readValue(json)
        return props.address
    }

    private fun buildClient(address: String): ApiClient {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$address")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()
        return retrofit.create(ApiClient::class.java)
    }
}