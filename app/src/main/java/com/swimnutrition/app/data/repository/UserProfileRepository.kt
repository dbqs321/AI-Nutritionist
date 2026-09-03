package com.swimnutrition.app.data.repository

import com.swimnutrition.app.data.service.SupabaseClient
import com.swimnutrition.app.domain.model.UserProfile
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json

class UserProfileRepository {
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun createUserProfile(profile: UserProfile): Result<UserProfile> {
        return try {
            val response = SupabaseClient.client.from("user_profiles")
                .insert(profile)
                .select()
                .decodeSingle<UserProfile>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        return try {
            val response = SupabaseClient.client.from("user_profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProfile(profile: UserProfile): Result<UserProfile> {
        return try {
            val response = SupabaseClient.client.from("user_profiles")
                .update(profile)
                .filter {
                    eq("id", profile.id)
                }
                .select()
                .decodeSingle<UserProfile>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}