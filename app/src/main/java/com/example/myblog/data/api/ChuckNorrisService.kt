package com.example.myblog.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


data class JokeResponse(
    val icon_url: String,
    val id: String,
    val url: String,
    val value: String
)


interface ChuckNorrisApi {
    @GET("jokes/random")
    suspend fun getRandomJoke(): JokeResponse
}

// Service Singleton
object ChuckNorrisService {
    private const val BASE_URL = "https://api.chucknorris.io/"

    private val client = OkHttpClient.Builder().build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ChuckNorrisApi = retrofit.create(ChuckNorrisApi::class.java)


    suspend fun fetchRandomJoke(): String {
        return try {
            val jokeResponse = api.getRandomJoke()
            jokeResponse.value
        } catch (e: Exception) {
            e.printStackTrace()
            "Error fetching joke: ${e.message}"
        }
    }
}