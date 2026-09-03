package com.swimnutrition.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    val age: Int,
    val sex: Sex,
    val height: Double, // cm
    val weight: Double, // kg
    val bodyCompositionGoal: BodyCompositionGoal,
    val sport: Sport,
    val swimmingEvents: List<String> = emptyList(),
    val trainingLoad: TrainingLoad,
    val dailySchedule: DailySchedule,
    val sleepPattern: SleepPattern,
    val nutritionGoals: NutritionGoals,
    val foodPreferences: FoodPreferences,
    val cookingConstraints: CookingConstraints,
    val supplementation: List<String> = emptyList(),
    val pastNutritionAttempts: List<NutritionAttempt> = emptyList()
)

@Serializable
enum class Sex {
    MALE, FEMALE, OTHER
}

@Serializable
enum class BodyCompositionGoal {
    MAINTAIN_WEIGHT, GAIN_MUSCLE, LOSE_FAT, OPTIMIZE_PERFORMANCE
}

@Serializable
data class Sport(
    val name: String = "Swimming",
    val level: String // "High School", "Club", "Collegiate", "Professional"
)

@Serializable
data class TrainingLoad(
    val weeklyYardage: Int, // total yards per week
    val intensityDistribution: Map<String, Int>, // "easy": 30, "moderate": 50, "hard": 20
    val keyWorkouts: List<String> = emptyList(),
    val restDays: List<String> = emptyList() // ["Monday", "Thursday"]
)

@Serializable
data class DailySchedule(
    val schoolHours: String, // "8:00 AM - 3:00 PM"
    val practiceTimes: String, // "4:00 PM - 6:00 PM"
    val commuteTime: Int, // minutes
    val homeworkLoad: HomeworkLoad
)

@Serializable
enum class HomeworkLoad {
    LIGHT, MODERATE, HEAVY
}

@Serializable
data class SleepPattern(
    val bedtime: String, // "10:30 PM"
    val wakeTime: String, // "6:00 AM"
    val quality: SleepQuality
)

@Serializable
enum class SleepQuality {
    POOR, FAIR, GOOD, EXCELLENT
}

@Serializable
data class NutritionGoals(
    val primaryGoal: BodyCompositionGoal,
    val calorieTarget: Int? = null,
    val proteinTarget: Int? = null, // grams
    val carbTarget: Int? = null, // grams
    val fatTarget: Int? = null // grams
)

@Serializable
data class FoodPreferences(
    val lovedFoods: List<String> = emptyList(),
    val dislikedFoods: List<String> = emptyList(),
    val culturalPractices: List<String> = emptyList(),
    val religiousRestrictions: List<String> = emptyList()
)

@Serializable
data class CookingConstraints(
    val skillLevel: CookingSkill,
    val availableEquipment: List<String> = emptyList(),
    val typicalCookingTime: Int // minutes
)

@Serializable
enum class CookingSkill {
    BEGINNER, INTERMEDIATE, ADVANCED
}

@Serializable
data class NutritionAttempt(
    val description: String,
    val whatWorked: String,
    val whatFailed: String,
    val why: String
)