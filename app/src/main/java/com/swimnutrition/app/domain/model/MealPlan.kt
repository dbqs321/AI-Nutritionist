package com.swimnutrition.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MealPlan(
    val id: String = "",
    val userId: String,
    val date: String, // ISO date
    val dailySummary: DailyNutritionSummary,
    val meals: List<Meal>,
    val groceryList: GroceryList,
    val prepStrategy: PrepStrategy,
    val educationNote: String
)

@Serializable
data class DailyNutritionSummary(
    val totalCalories: Int,
    val totalProtein: Int, // grams
    val totalCarbs: Int, // grams
    val totalFats: Int, // grams
    val rationale: String,
    val keyFocus: String
)

@Serializable
data class Meal(
    val id: String = "",
    val name: String,
    val description: String,
    val type: MealType,
    val ingredients: List<Ingredient>,
    val macros: Macros,
    val prepTime: Int, // minutes
    val difficulty: Difficulty,
    val makeAheadNotes: String? = null,
    val substitutions: List<String> = emptyList(),
    val timingGuidance: String
)

@Serializable
enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK, PRE_WORKOUT, POST_WORKOUT
}

@Serializable
data class Ingredient(
    val name: String,
    val quantityMetric: String, // "200g"
    val quantityImperial: String // "7oz"
)

@Serializable
data class Macros(
    val calories: Int,
    val protein: Int, // grams
    val carbs: Int, // grams
    val fats: Int // grams
)

@Serializable
enum class Difficulty {
    EASY, MODERATE, HARD
}

@Serializable
data class GroceryList(
    val produce: List<GroceryItem>,
    val proteins: List<GroceryItem>,
    val grains: List<GroceryItem>,
    val dairy: List<GroceryItem>,
    val pantry: List<GroceryItem>
)

@Serializable
data class GroceryItem(
    val name: String,
    val quantity: String,
    val budgetFriendlySwap: String? = null,
    val shelfLifeNotes: String? = null,
    val prepAhead: Boolean = false
)

@Serializable
data class PrepStrategy(
    val cookInAdvance: List<String>,
    val timeSavingHacks: List<String>,
    val storageInstructions: List<String>
)