package com.swimnutrition.app.di

import com.swimnutrition.app.agent.AIProvider
import com.swimnutrition.app.agent.MultiProviderAIManager
import com.swimnutrition.app.agent.NutritionAgentImpl
import com.swimnutrition.app.data.repository.MealPlanRepository
import com.swimnutrition.app.data.repository.UserProfileRepository
import com.swimnutrition.app.data.service.SecretsManager
import com.swimnutrition.app.domain.usecase.GenerateMealPlanUseCase
import com.swimnutrition.app.ui.screens.MealPlanViewModel

object AppModule {
    
    // AI Providers (configure with actual API keys)
    private val aiProviders = listOf<AIProvider>(
        AIProvider.OpenAI(SecretsManager.getOpenAIApiKey()),
        AIProvider.Anthropic(SecretsManager.getAnthropicApiKey())
        // Add more providers as needed
    )
    
    val aiManager: MultiProviderAIManager = MultiProviderAIManager(aiProviders)
    
    val userProfileRepository: UserProfileRepository = UserProfileRepository()
    
    val mealPlanRepository: MealPlanRepository = MealPlanRepository()
    
    val generateMealPlanUseCase: GenerateMealPlanUseCase = GenerateMealPlanUseCase(
        userProfileRepository = userProfileRepository,
        mealPlanRepository = mealPlanRepository,
        aiManager = aiManager
    )
    
    fun provideMealPlanViewModel(): MealPlanViewModel {
        return MealPlanViewModel(
            generateMealPlanUseCase = generateMealPlanUseCase,
            mealPlanRepository = mealPlanRepository,
            userProfileRepository = userProfileRepository,
            aiManager = aiManager
        )
    }
}