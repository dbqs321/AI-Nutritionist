package com.swimnutrition.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.swimnutrition.app.domain.model.*

@Composable
fun ProfileSetupScreen(navController: NavHostController) {
    // Basic Info
    var age by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf<Sex>(Sex.MALE) }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var bodyGoal by remember { mutableStateOf<BodyCompositionGoal>(BodyCompositionGoal.OPTIMIZE_PERFORMANCE) }
    
    // Sport Info
    var sportLevel by remember { mutableStateOf("High School") }
    var weeklyYardage by remember { mutableStateOf("") }
    var practiceTimes by remember { mutableStateOf("4:00 PM - 6:00 PM") }
    
    // Schedule
    var schoolHours by remember { mutableStateOf("8:00 AM - 3:00 PM") }
    var commuteTime by remember { mutableStateOf("15") }
    var homeworkLoad by remember { mutableStateOf<HomeworkLoad>(HomeworkLoad.MODERATE) }
    
    // Sleep
    var bedtime by remember { mutableStateOf("10:30 PM") }
    var wakeTime by remember { mutableStateOf("6:00 AM") }
    var sleepQuality by remember { mutableStateOf<SleepQuality>(SleepQuality.GOOD) }
    
    // Cooking
    var cookingSkill by remember { mutableStateOf<CookingSkill>(CookingSkill.INTERMEDIATE) }
    var cookingTime by remember { mutableStateOf("30") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Your Profile") },
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
                text = "Tell us about yourself",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Basic Information Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Basic Information", style = MaterialTheme.typography.titleMedium)
                    
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    var sexExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = sexExpanded,
                        onExpandedChange = { sexExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = sex.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sex") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = sexExpanded,
                            onDismissRequest = { sexExpanded = false }
                        ) {
                            Sex.values().forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        sex = s
                                        sexExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (cm)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    var goalExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = goalExpanded,
                        onExpandedChange = { goalExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = bodyGoal.name.replace("_", " "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Body Composition Goal") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = goalExpanded,
                            onDismissRequest = { goalExpanded = false }
                        ) {
                            BodyCompositionGoal.values().forEach { goal ->
                                DropdownMenuItem(
                                    text = { Text(goal.name.replace("_", " ")) },
                                    onClick = {
                                        bodyGoal = goal
                                        goalExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Sport Information Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sport Information", style = MaterialTheme.typography.titleMedium)
                    
                    OutlinedTextField(
                        value = sportLevel,
                        onValueChange = { sportLevel = it },
                        label = { Text("Sport Level") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = weeklyYardage,
                        onValueChange = { weeklyYardage = it },
                        label = { Text("Weekly Yardage") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = practiceTimes,
                        onValueChange = { practiceTimes = it },
                        label = { Text("Practice Times") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Schedule Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Daily Schedule", style = MaterialTheme.typography.titleMedium)
                    
                    OutlinedTextField(
                        value = schoolHours,
                        onValueChange = { schoolHours = it },
                        label = { Text("School Hours") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = commuteTime,
                        onValueChange = { commuteTime = it },
                        label = { Text("Commute Time (minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    var homeworkExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = homeworkExpanded,
                        onExpandedChange = { homeworkExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = homeworkLoad.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Homework Load") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeworkExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = homeworkExpanded,
                            onDismissRequest = { homeworkExpanded = false }
                        ) {
                            HomeworkLoad.values().forEach { load ->
                                DropdownMenuItem(
                                    text = { Text(load.name) },
                                    onClick = {
                                        homeworkLoad = load
                                        homeworkExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Sleep Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sleep Pattern", style = MaterialTheme.typography.titleMedium)
                    
                    OutlinedTextField(
                        value = bedtime,
                        onValueChange = { bedtime = it },
                        label = { Text("Bedtime") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = wakeTime,
                        onValueChange = { wakeTime = it },
                        label = { Text("Wake Time") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    var sleepExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = sleepExpanded,
                        onExpandedChange = { sleepExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = sleepQuality.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sleep Quality") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sleepExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = sleepExpanded,
                            onDismissRequest = { sleepExpanded = false }
                        ) {
                            SleepQuality.values().forEach { quality ->
                                DropdownMenuItem(
                                    text = { Text(quality.name) },
                                    onClick = {
                                        sleepQuality = quality
                                        sleepExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Cooking Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Cooking Constraints", style = MaterialTheme.typography.titleMedium)
                    
                    var cookingExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = cookingExpanded,
                        onExpandedChange = { cookingExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = cookingSkill.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cooking Skill Level") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cookingExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = cookingExpanded,
                            onDismissRequest = { cookingExpanded = false }
                        ) {
                            CookingSkill.values().forEach { skill ->
                                DropdownMenuItem(
                                    text = { Text(skill.name) },
                                    onClick = {
                                        cookingSkill = skill
                                        cookingExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = cookingTime,
                        onValueChange = { cookingTime = it },
                        label = { Text("Typical Cooking Time (minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    // TODO: Save profile and navigate back
                    navController.popBackStack() 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }
        }
    }
}