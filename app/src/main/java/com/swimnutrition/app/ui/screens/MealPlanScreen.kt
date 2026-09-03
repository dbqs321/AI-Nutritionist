package com.swimnutrition.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.swimnutrition.app.di.AppModule
import com.swimnutrition.app.domain.model.MealPlan
import java.time.LocalDate

@Composable
fun MealPlanScreen(navController: NavHostController) {
    val viewModel: MealPlanViewModel = viewModel { 
        AppModule.provideMealPlanViewModel() 
    }
    
    val mealPlan by viewModel.mealPlan.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Load meal plan when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadMealPlan(
            userId = "current-user-id", // TODO: Get actual user ID from auth
            date = LocalDate.now().toString()
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Meal Plan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                    Text("Loading your personalized meal plan...")
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.clearError() }) {
                        Text("Retry")
                    }
                }
                mealPlan != null -> {
                    MealPlanContent(mealPlan = mealPlan!!, viewModel = viewModel)
                }
                else -> {
                    Text("No meal plan available. Click to generate one.")
                    Button(onClick = { 
                        viewModel.loadMealPlan(
                            userId = "current-user-id",
                            date = LocalDate.now().toString()
                        )
                    }) {
                        Text("Generate Meal Plan")
                    }
                }
            }
        }
    }
}

@Composable
fun MealPlanContent(mealPlan: MealPlan, viewModel: MealPlanViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Daily Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily Nutrition Summary",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Calories: ${mealPlan.dailySummary.totalCalories}")
                Text("Protein: ${mealPlan.dailySummary.totalProtein}g")
                Text("Carbs: ${mealPlan.dailySummary.totalCarbs}g")
                Text("Fats: ${mealPlan.dailySummary.totalFats}g")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Focus: ${mealPlan.dailySummary.keyFocus}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Meals
        Text(
            text = "Today's Meals",
            style = MaterialTheme.typography.titleMedium
        )
        
        mealPlan.meals.forEach { meal ->
            MealCard(meal = meal)
        }
        
        // Education Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nutrition Insight",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mealPlan.educationNote,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun MealCard(meal: com.swimnutrition.app.domain.model.Meal) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = meal.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Prep: ${meal.prepTime} min | ${meal.difficulty}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Timing: ${meal.timingGuidance}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Macros: ${meal.macros.calories} cal | ${meal.macros.protein}g protein | ${meal.macros.carbs}g carbs | ${meal.macros.fats}g fat",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}