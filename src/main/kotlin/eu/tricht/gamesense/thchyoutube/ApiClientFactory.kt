package eu.tricht.gamesense.thchyoutube

import eu.tricht.gamesense.mapper
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class ApiClientFactory {
    companion object {
        fun createApiClient(): ApiClient? {
            try {
                val client = buildClient()
                client.ping().execute()
                return client
            } catch (e: Exception) {
                println("Couldn't connect to th-ch's youtube music api")
                return null
            }
        }

        private fun buildClient(): ApiClient {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://localhost:26538")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build()
            return retrofit.create(ApiClient::class.java)
        }
    }
}