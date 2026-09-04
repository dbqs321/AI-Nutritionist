package com.swimnutrition.app.data.service

import com.swimnutrition.app.domain.model.MealPlan
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class RAGBackendService(private val baseUrl: String = "http://localhost:5000") {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS) // Longer timeout for AI generation
        .build()
    
    suspend fun generateMealPlan(
        userId: String,
        trainingLoad: String = "moderate training",
        mealType: String = "daily",
        date: String? = null
    ): Result<Map<String, Any>> {
        return try {
            val jsonObject = JSONObject().apply {
                put("user_id", userId)
                put("training_load", trainingLoad)
                put("meal_type", mealType)
                date?.let { put("date", it) }
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/meal-plan/generate")
                .post(jsonObject.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("Backend error: ${response.code}")
            }
            
            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            
            if (jsonResponse.optBoolean("success", false)) {
                val mealPlanData = jsonResponse.optJSONObject("meal_plan")?.toMap() ?: emptyMap()
                Result.success(mealPlanData)
            } else {
                Result.failure(Exception(jsonResponse.optString("error", "Unknown error")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun adjustMealPlan(
        mealPlanId: String,
        newTrainingLoad: String
    ): Result<Map<String, Any>> {
        return try {
            val jsonObject = JSONObject().apply {
                put("meal_plan_id", mealPlanId)
                put("new_training_load", newTrainingLoad)
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/meal-plan/adjust")
                .post(jsonObject.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("Backend error: ${response.code}")
            }
            
            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            
            if (jsonResponse.optBoolean("success", false)) {
                val mealPlanData = jsonResponse.optJSONObject("meal_plan")?.toMap() ?: emptyMap()
                Result.success(mealPlanData)
            } else {
                Result.failure(Exception(jsonResponse.optString("error", "Unknown error")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun handleCompetitionDay(
        userId: String,
        eventTime: String
    ): Result<Map<String, Any>> {
        return try {
            val jsonObject = JSONObject().apply {
                put("user_id", userId)
                put("event_time", eventTime)
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/meal-plan/competition")
                .post(jsonObject.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("Backend error: ${response.code}")
            }
            
            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            
            if (jsonResponse.optBoolean("success", false)) {
                val mealPlanData = jsonResponse.optJSONObject("meal_plan")?.toMap() ?: emptyMap()
                Result.success(mealPlanData)
            } else {
                Result.failure(Exception(jsonResponse.optString("error", "Unknown error")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun handleTravelScenario(
        userId: String,
        destination: String
    ): Result<Map<String, Any>> {
        return try {
            val jsonObject = JSONObject().apply {
                put("user_id", userId)
                put("destination", destination)
            }
            
            val request = Request.Builder()
                .url("$baseUrl/api/meal-plan/travel")
                .post(jsonObject.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("Backend error: ${response.code}")
            }
            
            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            
            if (jsonResponse.optBoolean("success", false)) {
                val mealPlanData = jsonResponse.optJSONObject("meal_plan")?.toMap() ?: emptyMap()
                Result.success(mealPlanData)
            } else {
                Result.failure(Exception(jsonResponse.optString("error", "Unknown error")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkHealth(): Result<Boolean> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getKnowledgeBaseStatus(): Result<Map<String, Any>> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/api/knowledge-base/status")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("Backend error: ${response.code}")
            }
            
            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            
            if (jsonResponse.optBoolean("success", false)) {
                val statusData = jsonResponse.optJSONObject("status")?.toMap() ?: emptyMap()
                Result.success(statusData)
            } else {
                Result.failure(Exception(jsonResponse.optString("error", "Unknown error")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension function to convert JSONObject to Map
fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val value = this.get(key)
        when (value) {
            is JSONObject -> map[key] = value.toMap()
            is JSONArray -> map[key] = value.toList()
            else -> map[key] = value
        }
    }
    return map
}

// Extension function to convert JSONArray to List
fun JSONArray.toList(): List<Any> {
    val list = mutableListOf<Any>()
    for (i in 0 until this.length()) {
        val value = this.get(i)
        when (value) {
            is JSONObject -> list.add(value.toMap())
            is JSONArray -> list.add(value.toList())
            else -> list.add(value)
        }
    }
    return list
}