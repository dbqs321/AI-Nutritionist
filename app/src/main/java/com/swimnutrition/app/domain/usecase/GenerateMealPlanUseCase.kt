package com.swimnutrition.app.domain.usecase

import com.swimnutrition.app.agent.NutritionAgent
import com.swimnutrition.app.agent.NutritionAgentImpl
import com.swimnutrition.app.agent.MultiProviderAIManager
import com.swimnutrition.app.data.repository.MealPlanRepository
import com.swimnutrition.app.data.repository.UserProfileRepository
import com.swimnutrition.app.domain.model.MealPlan
import com.swimnutrition.app.domain.model.UserProfile

class GenerateMealPlanUseCase(
    private val userProfileRepository: UserProfileRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val aiManager: MultiProviderAIManager
) {
    private val nutritionAgent: NutritionAgent = NutritionAgentImpl(aiManager)
    
    suspend operator fun invoke(userId: String, date: String): Result<MealPlan> {
        return try {
            // Get user profile
            val profileResult = userProfileRepository.getUserProfile(userId)
            val profile = profileResult.getOrNull() ?: return Result.failure(
                Exception("User profile not found")
            )
            
            // Generate meal plan using AI agent
            val mealPlan = nutritionAgent.generateMealPlan(profile, date)
            
            // Save meal plan to database
            val savedMealPlan = mealPlanRepository.createMealPlan(mealPlan)
            
            savedMealPlan
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun adjustForTrainingLoad(
        mealPlanId: String,
        trainingIntensity: String
    ): Result<MealPlan> {
        return try {
            val mealPlan = nutritionAgent.adjustForTrainingLoad(
                mealPlan.copy(id = mealPlanId),
                trainingIntensity
            )
            
            mealPlanRepository.updateMealPlan(mealPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun handleCompetitionDay(
        userId: String,
        eventTime: String
    ): Result<MealPlan> {
        return try {
            val profileResult = userProfileRepository.getUserProfile(userId)
            val profile = profileResult.getOrNull() ?: return Result.failure(
                Exception("User profile not found")
            )
            
            val mealPlan = nutritionAgent.handleCompetitionDay(profile, eventTime)
            mealPlanRepository.createMealPlan(mealPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}