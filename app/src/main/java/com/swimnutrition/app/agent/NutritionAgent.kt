package com.swimnutrition.app.agent

import com.swimnutrition.app.domain.model.MealPlan
import com.swimnutrition.app.domain.model.UserProfile

interface NutritionAgent {
    suspend fun generateMealPlan(profile: UserProfile, date: String): MealPlan
    suspend fun adjustForTrainingLoad(mealPlan: MealPlan, trainingIntensity: String): MealPlan
    suspend fun handleCompetitionDay(profile: UserProfile, eventTime: String): MealPlan
    suspend fun handleTravelScenario(profile: UserProfile, destination: String): MealPlan
    suspend fun getFeedbackRecommendations(feedback: UserFeedback): String
}

data class UserFeedback(
    val mealPlanId: String,
    val appealingMeals: List<String>,
    val unrealisticMeals: List<String>,
    val confidenceLevel: Int, // 1-10
    val anticipatedBarriers: List<String>,
    val learningsFromPreviousWeek: String
)