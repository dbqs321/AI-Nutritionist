package com.swimnutrition.app.agent

import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@Serializable
data class OpenAIRequest(
    val model: String = "gpt-4",
    val messages: List<Message>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 2000
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: Message
)

class OpenAIService(private val apiKey: String) {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    suspend fun generateMealPlan(prompt: String): String {
        val request = OpenAIRequest(
            messages = listOf(
                Message("system", "You are an expert sports nutritionist specializing in competitive swimmers."),
                Message("user", prompt)
            )
        )
        
        val jsonRequest = """
            {
                "model": "gpt-4",
                "messages": [
                    {"role": "system", "content": "You are an expert sports nutritionist specializing in competitive swimmers."},
                    {"role": "user", "content": ${prompt.replace("\"", "\\\"")}}
                ],
                "temperature": 0.7,
                "max_tokens": 2000
            }
        """.trimIndent()
        
        val httpRequest = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonRequest.toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(httpRequest).execute()
        if (!response.isSuccessful) {
            throw Exception("OpenAI API error: ${response.code} ${response.message}")
        }
        
        return response.body?.string() ?: ""
    }
}