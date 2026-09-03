package com.swimnutrition.app.data.service

object SecretsManager {
    // In production, these should be loaded from secure storage
    // For development, you can set them here or use local.properties
    
    fun getSupabaseUrl(): String {
        return System.getenv("SUPABASE_URL") 
            ?: "YOUR_SUPABASE_URL"
    }
    
    fun getSupabaseAnonKey(): String {
        return System.getenv("SUPABASE_ANON_KEY") 
            ?: "YOUR_SUPABASE_ANON_KEY"
    }
    
    fun getOpenAIApiKey(): String {
        return System.getenv("OPENAI_API_KEY") 
            ?: "YOUR_OPENAI_API_KEY"
    }
    
    fun getAnthropicApiKey(): String {
        return System.getenv("ANTHROPIC_API_KEY") 
            ?: "YOUR_ANTHROPIC_API_KEY"
    }
}