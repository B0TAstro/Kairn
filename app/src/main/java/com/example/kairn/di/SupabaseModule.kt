package com.example.kairn.di

import android.content.Context
import com.example.kairn.BuildConfig
import io.github.jan-tennert.supabase.SupabaseClient
import io.github.jan-tennert.supabase.gotrue.Auth
import io.github.jan-tennert.supabase.gotrue.FlowType
import io.github.jan-tennert.supabase.postgrest.Postgrest
import io.github.jan-tennert.supabase.realtime.Realtime
import io.github.jan-tennert.supabase.storage.Storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.android.Android
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(@ApplicationContext context: Context): SupabaseClient {
        return SupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
            httpEngine = Android.create()
        ) {
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "com.example.kairn"
                host = "auth"
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth = client.auth

    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    @Provides
    @Singleton
    fun provideRealtime(client: SupabaseClient): Realtime = client.realtime

    @Provides
    @Singleton
    fun provideStorage(client: SupabaseClient): Storage = client.storage
}