package com.swimnutrition.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.swimnutrition.app.ui.screens.HomeScreen
import com.swimnutrition.app.ui.screens.ProfileSetupScreen
import com.swimnutrition.app.ui.screens.MealPlanScreen
import com.swimnutrition.app.ui.screens.GroceryListScreen
import com.swimnutrition.app.ui.screens.TrackingScreen
import com.swimnutrition.app.ui.screens.FeedbackScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ProfileSetup : Screen("profile_setup")
    object MealPlan : Screen("meal_plan")
    object GroceryList : Screen("grocery_list")
    object Tracking : Screen("tracking")
    object Feedback : Screen("feedback/{mealPlanId}") {
        fun createRoute(mealPlanId: String) = "feedback/$mealPlanId"
    }
}

@Composable
fun SwimNutritionNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(navController = navController)
        }
        composable(Screen.MealPlan.route) {
            MealPlanScreen(navController = navController)
        }
        composable(Screen.GroceryList.route) {
            GroceryListScreen(navController = navController)
        }
        composable(Screen.Tracking.route) {
            TrackingScreen(navController = navController)
        }
        composable(Screen.Feedback.route) { backStackEntry ->
            val mealPlanId = backStackEntry.arguments?.getString("mealPlanId") ?: ""
            FeedbackScreen(navController = navController, mealPlanId = mealPlanId)
        }
    }
}