package com.example.kairn.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.Storage
import java.util.Properties

object SupabaseClient {
    private val client: SupabaseClient by lazy {
        val properties = Properties()
        val envFile = System.getenv("ENV_FILE")?.let { java.io.File(it) } 
            ?: try {
                java.io.File(".env")
            } catch (e: Exception) {
                null
            }
        
        envFile?.let { file ->
            if (file.exists()) {
                file.inputStream().use { properties.load(it) }
            }
        }
        
        val supabaseUrl = properties.getProperty("SUPABASE_URL")
            ?: System.getenv("SUPABASE_URL")
            ?: "https://your-supabase-url.supabase.co"
        
        val supabaseAnonKey = properties.getProperty("SUPABASE_ANON_KEY")
            ?: System.getenv("SUPABASE_ANON_KEY")
            ?: "your-supabase-anon-key"
        
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            install(GoTrue)
            install(Storage)
        }
    }
    
    fun getClient(): SupabaseClient = client
    
    fun getStorage(): Storage = client.storage
}