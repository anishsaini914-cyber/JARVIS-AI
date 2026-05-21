package com.jarvis.assistant.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    @SecurePreferences
    fun provideEncryptedPreferences(@ApplicationContext context: Context): android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "jarvis_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    @AppPreferences
    fun provideAppPreferences(@ApplicationContext context: Context): android.content.SharedPreferences {
        return context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)
    }
}

@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SecurePreferences

@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppPreferences
