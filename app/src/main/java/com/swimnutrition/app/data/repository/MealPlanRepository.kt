package com.swimnutrition.app.data.repository

import com.swimnutrition.app.data.service.SupabaseClient
import com.swimnutrition.app.domain.model.MealPlan
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json

class MealPlanRepository {
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun createMealPlan(mealPlan: MealPlan): Result<MealPlan> {
        return try {
            val response = SupabaseClient.client.from("meal_plans")
                .insert(mealPlan)
                .select()
                .decodeSingle<MealPlan>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMealPlan(userId: String, date: String): Result<MealPlan?> {
        return try {
            val response = SupabaseClient.client.from("meal_plans")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("date", date)
                    }
                }
                .decodeSingle<MealPlan>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getWeeklyMealPlans(userId: String, startDate: String, endDate: String): Result<List<MealPlan>> {
        return try {
            val response = SupabaseClient.client.from("meal_plans")
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("date", startDate)
                        lte("date", endDate)
                    }
                }
                .decodeList<MealPlan>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateMealPlan(mealPlan: MealPlan): Result<MealPlan> {
        return try {
            val response = SupabaseClient.client.from("meal_plans")
                .update(mealPlan)
                .filter {
                    eq("id", mealPlan.id)
                }
                .select()
                .decodeSingle<MealPlan>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}