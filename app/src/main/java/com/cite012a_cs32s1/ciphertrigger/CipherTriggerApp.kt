package com.cite012a_cs32s1.ciphertrigger

import android.app.Application
import androidx.lifecycle.lifecycleScope
import com.cite012a_cs32s1.ciphertrigger.di.AppModule
import com.cite012a_cs32s1.ciphertrigger.services.VoiceRecognitionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Application class for CipherTrigger
 */
class CipherTriggerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the dependency injection
        AppModule.initialize(applicationContext)

        // Initialize voice recognition service if enabled
        val preferencesRepository = AppModule.providePreferencesRepository(this)

        // Use applicationScope to launch coroutines in the application scope
        val applicationScope = lifecycleScope
        applicationScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()
            if (preferences.isSetupCompleted && preferences.voiceTriggerEnabled) {
                VoiceRecognitionManager.initialize(this@CipherTriggerApp, preferencesRepository)
            }
        }
    }
}
