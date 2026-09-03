package com.swimnutrition.app.agent

import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@Serializable
data class AnthropicRequest(
    val model: String = "claude-3-sonnet-20240229",
    val max_tokens: Int = 2000,
    val messages: List<AnthropicMessage>
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: String
)

@Serializable
data class AnthropicResponse(
    val content: List<ContentBlock>
)

@Serializable
data class ContentBlock(
    val type: String,
    val text: String
)

class AnthropicService(private val apiKey: String) {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    suspend fun generateMealPlan(prompt: String): String {
        val jsonRequest = """
            {
                "model": "claude-3-sonnet-20240229",
                "max_tokens": 2000,
                "messages": [
                    {"role": "user", "content": "You are an expert sports nutritionist specializing in competitive swimmers. ${prompt}"}
                ]
            }
        """.trimIndent()
        
        val httpRequest = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(jsonRequest.toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(httpRequest).execute()
        if (!response.isSuccessful) {
            throw Exception("Anthropic API error: ${response.code} ${response.message}")
        }
        
        return response.body?.string() ?: ""
    }
}