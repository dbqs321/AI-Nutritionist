package com.swimnutrition.app.agent

import com.swimnutrition.app.data.service.RAGBackendService
import com.swimnutrition.app.domain.model.MealPlan
import com.swimnutrition.app.domain.model.UserProfile
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NutritionAgentImpl(
    private val aiManager: MultiProviderAIManager,
    private val ragBackendService: RAGBackendService = RAGBackendService()
) : NutritionAgent {
    
    private val json = Json { ignoreUnknownKeys = true }
    private var useRAGBackend = true
    
    override suspend fun generateMealPlan(profile: UserProfile, date: String): MealPlan {
        return if (useRAGBackend) {
            generateMealPlanWithRAG(profile, date)
        } else {
            generateMealPlanDirect(profile, date)
        }
    }
    
    private suspend fun generateMealPlanWithRAG(profile: UserProfile, date: String): MealPlan {
        return try {
            // Call RAG backend for evidence-based meal plan
            val result = ragBackendService.generateMealPlan(
                userId = profile.id,
                trainingLoad = buildTrainingLoadDescription(profile),
                mealType = "daily",
                date = date
            )
            
            if (result.isSuccess) {
                parseRAGResponse(result.getOrNull() ?: emptyMap(), profile.id, date)
            } else {
                // Fallback to direct AI if backend fails
                println("RAG backend failed, falling back to direct AI: ${result.exceptionOrNull()?.message}")
                useRAGBackend = false
                generateMealPlanDirect(profile, date)
            }
        } catch (e: Exception) {
            println("RAG backend error, falling back to direct AI: ${e.message}")
            useRAGBackend = false
            generateMealPlanDirect(profile, date)
        }
    }
    
    private suspend fun generateMealPlanDirect(profile: UserProfile, date: String): MealPlan {
        val prompt = buildString {
            append("Generate a personalized meal plan for a competitive swimmer.\n\n")
            append("USER PROFILE:\n")
            append(json.encodeToString(profile))
            append("\n\nDATE: $date")
            append("\n\nPlease provide a complete meal plan following the nutrition planner skill format.")
        }
        
        val response = callAI(prompt)
        return parseMealPlanResponse(response)
    }
    
    override suspend fun adjustForTrainingLoad(mealPlan: MealPlan, trainingIntensity: String): MealPlan {
        return if (useRAGBackend) {
            try {
                val result = ragBackendService.adjustMealPlan(
                    mealPlanId = mealPlan.id,
                    newTrainingLoad = trainingIntensity
                )
                
                if (result.isSuccess) {
                    parseRAGResponse(result.getOrNull() ?: emptyMap(), mealPlan.userId, mealPlan.date)
                } else {
                    adjustForTrainingLoadDirect(mealPlan, trainingIntensity)
                }
            } catch (e: Exception) {
                adjustForTrainingLoadDirect(mealPlan, trainingIntensity)
            }
        } else {
            adjustForTrainingLoadDirect(mealPlan, trainingIntensity)
        }
    }
    
    private suspend fun adjustForTrainingLoadDirect(mealPlan: MealPlan, trainingIntensity: String): MealPlan {
        val prompt = buildString {
            append("Adjust this meal plan for training intensity: $trainingIntensity\n\n")
            append("CURRENT MEAL PLAN:\n")
            append(json.encodeToString(mealPlan))
            append("\n\nModify calorie and carbohydrate targets accordingly.")
        }
        
        val response = callAI(prompt)
        return parseMealPlanResponse(response)
    }
    
    override suspend fun handleCompetitionDay(profile: UserProfile, eventTime: String): MealPlan {
        return if (useRAGBackend) {
            try {
                val result = ragBackendService.handleCompetitionDay(
                    userId = profile.id,
                    eventTime = eventTime
                )
                
                if (result.isSuccess) {
                    parseRAGResponse(result.getOrNull() ?: emptyMap(), profile.id, java.time.LocalDate.now().toString())
                } else {
                    handleCompetitionDayDirect(profile, eventTime)
                }
            } catch (e: Exception) {
                handleCompetitionDayDirect(profile, eventTime)
            }
        } else {
            handleCompetitionDayDirect(profile, eventTime)
        }
    }
    
    private suspend fun handleCompetitionDayDirect(profile: UserProfile, eventTime: String): MealPlan {
        val prompt = buildString {
            append("Create a competition day meal plan for event time: $eventTime\n\n")
            append("USER PROFILE:\n")
            append(json.encodeToString(profile))
            append("\n\nFocus on pre-race fueling, during-meet nutrition, and recovery.")
        }
        
        val response = callAI(prompt)
        return parseMealPlanResponse(response)
    }
    
    override suspend fun handleTravelScenario(profile: UserProfile, destination: String): MealPlan {
        return if (useRAGBackend) {
            try {
                val result = ragBackendService.handleTravelScenario(
                    userId = profile.id,
                    destination = destination
                )
                
                if (result.isSuccess) {
                    parseRAGResponse(result.getOrNull() ?: emptyMap(), profile.id, java.time.LocalDate.now().toString())
                } else {
                    handleTravelScenarioDirect(profile, destination)
                }
            } catch (e: Exception) {
                handleTravelScenarioDirect(profile, destination)
            }
        } else {
            handleTravelScenarioDirect(profile, destination)
        }
    }
    
    private suspend fun handleTravelScenarioDirect(profile: UserProfile, destination: String): MealPlan {
        val prompt = buildString {
            append("Create a travel-friendly meal plan for destination: $destination\n\n")
            append("USER PROFILE:\n")
            append(json.encodeToString(profile))
            append("\n\nFocus on hotel-friendly options, restaurant strategies, and portable foods.")
        }
        
        val response = callAI(prompt)
        return parseMealPlanResponse(response)
    }
    
    override suspend fun getFeedbackRecommendations(feedback: UserFeedback): String {
        val prompt = buildString {
            append("User feedback on meal plan:\n")
            append(json.encodeToString(feedback))
            append("\n\nProvide recommendations to improve future meal plans based on this feedback.")
        }
        
        return callAI(prompt)
    }
    
    private suspend fun callAI(prompt: String): String {
        val provider = aiManager.getCurrentProvider()
        
        return withContext(Dispatchers.IO) {
            when (provider) {
                is AIProvider.OpenAI -> callOpenAI(provider.apiKey, prompt)
                is AIProvider.Anthropic -> callAnthropic(provider.apiKey, prompt)
                is AIProvider.OpenSource -> callOpenSource(provider.apiKey, provider.endpoint, prompt)
            }
        }
    }
    
    private suspend fun callOpenAI(apiKey: String, prompt: String): String {
        val service = OpenAIService(apiKey)
        return service.generateMealPlan(prompt)
    }
    
    private suspend fun callAnthropic(apiKey: String, prompt: String): String {
        val service = AnthropicService(apiKey)
        return service.generateMealPlan(prompt)
    }
    
    private suspend fun callOpenSource(apiKey: String, endpoint: String, prompt: String): String {
        // For open source models, we would implement a similar service
        // This could be for self-hosted Llama, Mistral, etc.
        // TODO: Implement open source API integration
        return "Open source integration not yet implemented"
    }
    
    private fun parseMealPlanResponse(response: String): MealPlan {
        // Parse AI response into MealPlan object
        // TODO: Add proper parsing logic
        return MealPlan(
            id = "",
            userId = "",
            date = "",
            dailySummary = com.swimnutrition.app.domain.model.DailyNutritionSummary(
                totalCalories = 2500,
                totalProtein = 150,
                totalCarbs = 300,
                totalFats = 80,
                rationale = "Based on training load",
                keyFocus = "Performance optimization"
            ),
            meals = emptyList(),
            groceryList = com.swimnutrition.app.domain.model.GroceryList(
                produce = emptyList(),
                proteins = emptyList(),
                grains = emptyList(),
                dairy = emptyList(),
                pantry = emptyList()
            ),
            prepStrategy = com.swimnutrition.app.domain.model.PrepStrategy(
                cookInAdvance = emptyList(),
                timeSavingHacks = emptyList(),
                storageInstructions = emptyList()
            ),
            educationNote = "Focus on timing nutrients around workouts"
        )
    }
    
    private fun parseRAGResponse(response: Map<String, Any>, userId: String, date: String): MealPlan {
        // Parse RAG backend response into MealPlan object
        // The RAG backend returns structured data with evidence citations
        val generatedText = response["generated_text"] as? String ?: ""
        val containsCitations = response["contains_citations"] as? Boolean ?: false
        
        // TODO: Implement proper parsing of the RAG response
        // For now, return a placeholder with the generated text
        return MealPlan(
            id = response["id"] as? String ?: "",
            userId = userId,
            date = date,
            dailySummary = com.swimnutrition.app.domain.model.DailyNutritionSummary(
                totalCalories = 2500,
                totalProtein = 150,
                totalCarbs = 300,
                totalFats = 80,
                rationale = if (containsCitations) "Evidence-based with citations" else "Based on training load",
                keyFocus = "Performance optimization with RAG"
            ),
            meals = emptyList(),
            groceryList = com.swimnutrition.app.domain.model.GroceryList(
                produce = emptyList(),
                proteins = emptyList(),
                grains = emptyList(),
                dairy = emptyList(),
                pantry = emptyList()
            ),
            prepStrategy = com.swimnutrition.app.domain.model.PrepStrategy(
                cookInAdvance = emptyList(),
                timeSavingHacks = emptyList(),
                storageInstructions = emptyList()
            ),
            educationNote = if (containsCitations) "Evidence-based recommendations with source citations" else "AI-generated meal plan"
        )
    }
    
    private fun buildTrainingLoadDescription(profile: UserProfile): String {
        return buildString {
            append("${profile.trainingLoad.weeklyYardage} yards weekly")
            append(" with intensity distribution: ")
            profile.trainingLoad.intensityDistribution.forEach { (intensity, percentage) ->
                append("$intensity ($percentage%), ")
            }
            append("practice times: ${profile.dailySchedule.practiceTimes}")
        }
    }
    
    suspend fun checkRAGBackendHealth(): Boolean {
        return try {
            val result = ragBackendService.checkHealth()
            result.isSuccess && result.getOrNull() == true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getKnowledgeBaseStatus(): Map<String, Any>? {
        return try {
            val result = ragBackendService.getKnowledgeBaseStatus()
            if (result.isSuccess) {
                result.getOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}