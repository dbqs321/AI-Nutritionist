package com.swimnutrition.app.data.service

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    private val SUPABASE_URL = SecretsManager.getSupabaseUrl()
    private val SUPABASE_ANON_KEY = SecretsManager.getSupabaseAnonKey()
    
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
    }
}