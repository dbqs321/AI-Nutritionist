package com.swimnutrition.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimnutrition.app.agent.MultiProviderAIManager
import com.swimnutrition.app.agent.AIProvider
import com.swimnutrition.app.data.repository.MealPlanRepository
import com.swimnutrition.app.data.repository.UserProfileRepository
import com.swimnutrition.app.domain.model.MealPlan
import com.swimnutrition.app.domain.usecase.GenerateMealPlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealPlanViewModel(
    private val generateMealPlanUseCase: GenerateMealPlanUseCase,
    private val mealPlanRepository: MealPlanRepository,
    private val userProfileRepository: UserProfileRepository,
    private val aiManager: MultiProviderAIManager
) : ViewModel() {
    
    private val _mealPlan = MutableStateFlow<MealPlan?>(null)
    val mealPlan: StateFlow<MealPlan?> = _mealPlan.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadMealPlan(userId: String, date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Try to get existing meal plan
                val existingPlan = mealPlanRepository.getMealPlan(userId, date)
                if (existingPlan.isSuccess && existingPlan.getOrNull() != null) {
                    _mealPlan.value = existingPlan.getOrNull()
                } else {
                    // Generate new meal plan
                    val result = generateMealPlanUseCase(userId, date)
                    if (result.isSuccess) {
                        _mealPlan.value = result.getOrNull()
                    } else {
                        _error.value = result.exceptionOrNull()?.message
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun adjustForTrainingLoad(mealPlanId: String, intensity: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = generateMealPlanUseCase.adjustForTrainingLoad(mealPlanId, intensity)
                if (result.isSuccess) {
                    _mealPlan.value = result.getOrNull()
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun handleCompetitionDay(userId: String, eventTime: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = generateMealPlanUseCase.handleCompetitionDay(userId, eventTime)
                if (result.isSuccess) {
                    _mealPlan.value = result.getOrNull()
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun switchAIProvider(providerName: String) {
        aiManager.switchToProvider(providerName)
    }
    
    fun clearError() {
        _error.value = null
    }
}