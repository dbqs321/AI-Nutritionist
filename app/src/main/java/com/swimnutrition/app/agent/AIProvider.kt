package com.swimnutrition.app.agent

sealed class AIProvider {
    abstract val name: String
    abstract val apiKey: String
    
    data class OpenAI(override val apiKey: String) : AIProvider() {
        override val name = "OpenAI"
    }
    
    data class Anthropic(override val apiKey: String) : AIProvider() {
        override val name = "Anthropic"
    }
    
    data class OpenSource(
        override val apiKey: String,
        val endpoint: String
    ) : AIProvider() {
        override val name = "OpenSource"
    }
}

class MultiProviderAIManager(
    private val providers: List<AIProvider>
) {
    private var currentProviderIndex = 0
    
    fun getCurrentProvider(): AIProvider {
        return providers[currentProviderIndex]
    }
    
    fun switchProvider() {
        currentProviderIndex = (currentProviderIndex + 1) % providers.size
    }
    
    fun switchToProvider(providerName: String) {
        val index = providers.indexOfFirst { it.name == providerName }
        if (index >= 0) {
            currentProviderIndex = index
        }
    }
    
    fun getAvailableProviders(): List<String> {
        return providers.map { it.name }
    }
}