package com.swimnutrition.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.swimnutrition.app.agent.UserFeedback

@Composable
fun FeedbackScreen(navController: NavHostController, mealPlanId: String) {
    var appealingMeals by remember { mutableStateOf("") }
    var unrealisticMeals by remember { mutableStateOf("") }
    var confidenceLevel by remember { mutableStateOf(7) }
    var barriers by remember { mutableStateOf("") }
    var learnings by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Plan Feedback") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Help us improve your meal plans",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Your feedback helps us create better personalized nutrition plans.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Appealing Meals", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = appealingMeals,
                        onValueChange = { appealingMeals = it },
                        label = { Text("Which meals did you enjoy? (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Unrealistic Meals", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = unrealisticMeals,
                        onValueChange = { unrealisticMeals = it },
                        label = { Text("Which meals felt unrealistic? (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Confidence Level", style = MaterialTheme.typography.titleMedium)
                    Text("How confident are you in following this plan? (1-10)")
                    
                    Slider(
                        value = confidenceLevel.toFloat(),
                        onValueChange = { confidenceLevel = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Confidence: $confidenceLevel/10",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Anticipated Barriers", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = barriers,
                        onValueChange = { barriers = it },
                        label = { Text("What challenges do you anticipate? (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Learnings", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = learnings,
                        onValueChange = { learnings = it },
                        label = { Text("What did you learn from last week's plan?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val feedback = UserFeedback(
                        mealPlanId = mealPlanId,
                        appealingMeals = appealingMeals.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        unrealisticMeals = unrealisticMeals.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        confidenceLevel = confidenceLevel,
                        anticipatedBarriers = barriers.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        learningsFromPreviousWeek = learnings
                    )
                    // TODO: Submit feedback and navigate back
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Feedback")
            }
        }
    }
}