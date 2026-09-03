package com.swimnutrition.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.swimnutrition.app.ui.navigation.Screen

@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Swim Nutrition") },
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
            Text(
                text = "Welcome to Your Personal Sports Nutritionist",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Get personalized meal plans optimized for your swimming training and competition schedule.",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { navController.navigate(Screen.ProfileSetup.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Setup Your Profile")
            }
            
            Button(
                onClick = { navController.navigate(Screen.MealPlan.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Today's Meal Plan")
            }
            
            Button(
                onClick = { navController.navigate(Screen.GroceryList.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grocery List")
            }
            
            Button(
                onClick = { navController.navigate(Screen.Tracking.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Track Your Nutrition")
            }
        }
    }
}