package com.cite012a_cs32s1.ciphertrigger.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Manager for the voice recognition service
 */
object VoiceRecognitionManager {
    private const val TAG = "VoiceRecognitionManager"
    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * Initialize the voice recognition service
     */
    fun initialize(context: Context, preferencesRepository: PreferencesRepository) {
        managerScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()
            
            if (preferences.voiceTriggerEnabled) {
                startVoiceRecognition(context)
            }
        }
    }
    
    /**
     * Start the voice recognition service
     */
    fun startVoiceRecognition(context: Context) {
        Log.d(TAG, "Starting voice recognition service")
        VoiceRecognitionService.startService(context)
    }
    
    /**
     * Stop the voice recognition service
     */
    fun stopVoiceRecognition(context: Context) {
        Log.d(TAG, "Stopping voice recognition service")
        VoiceRecognitionService.stopService(context)
    }
    
    /**
     * Update the voice recognition service based on user preferences
     */
    fun updateVoiceRecognition(context: Context, enabled: Boolean) {
        if (enabled) {
            startVoiceRecognition(context)
        } else {
            stopVoiceRecognition(context)
        }
    }
}
