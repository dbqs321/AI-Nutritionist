package com.swimnutrition.app.domain.usecase

import com.swimnutrition.app.agent.NutritionAgent
import com.swimnutrition.app.agent.NutritionAgentImpl
import com.swimnutrition.app.agent.MultiProviderAIManager
import com.swimnutrition.app.agent.UserFeedback
import com.swimnutrition.app.data.repository.UserProfileRepository

class SubmitFeedbackUseCase(
    private val userProfileRepository: UserProfileRepository,
    private val aiManager: MultiProviderAIManager
) {
    private val nutritionAgent: NutritionAgent = NutritionAgentImpl(aiManager)
    
    suspend operator fun invoke(feedback: UserFeedback): Result<String> {
        return try {
            val recommendations = nutritionAgent.getFeedbackRecommendations(feedback)
            Result.success(recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun generateImprovedPlan(
        userId: String,
        date: String,
        feedback: UserFeedback
    ): Result<String> {
        return try {
            val profileResult = userProfileRepository.getUserProfile(userId)
            val profile = profileResult.getOrNull() ?: return Result.failure(
                Exception("User profile not found")
            )
            
            val improvedPrompt = buildString {
                append("Generate an improved meal plan based on user feedback:\n")
                append("FEEDBACK:\n")
                append("Appealing meals: ${feedback.appealingMeals.joinToString(", ")}\n")
                append("Unrealistic meals: ${feedback.unrealisticMeals.joinToString(", ")}\n")
                append("Confidence level: ${feedback.confidenceLevel}/10\n")
                append("Barriers: ${feedback.anticipatedBarriers.joinToString(", ")}\n")
                append("Learnings: ${feedback.learningsFromPreviousWeek}\n")
                append("\nUSER PROFILE:\n")
                append(profile)
            }
            
            // This would call the AI with the improved prompt
            Result.success("Improved plan recommendations generated")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}