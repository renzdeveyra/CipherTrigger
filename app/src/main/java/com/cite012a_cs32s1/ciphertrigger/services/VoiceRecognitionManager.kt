package com.cite012a_cs32s1.ciphertrigger.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository
import com.cite012a_cs32s1.ciphertrigger.utils.MicrophoneStateManager
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionUtils
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

            // Check microphone permission and availability before starting
            val hasMicPermission = PermissionUtils.hasPermission(context, android.Manifest.permission.RECORD_AUDIO)
            val isMicAvailable = MicrophoneStateManager.checkMicrophoneAvailability(context)

            if (preferences.voiceTriggerEnabled && hasMicPermission && isMicAvailable) {
                // Keep microphone active when voice trigger is enabled
                MicrophoneStateManager.keepMicrophoneActive()
                startVoiceRecognition(context)
            }
        }
    }

    /**
     * Start the voice recognition service
     */
    fun startVoiceRecognition(context: Context) {
        Log.d(TAG, "Starting voice recognition service")
        // Keep microphone active when voice trigger is enabled
        MicrophoneStateManager.keepMicrophoneActive()
        VoiceRecognitionService.startService(context)
    }

    /**
     * Stop the voice recognition service
     */
    fun stopVoiceRecognition(context: Context) {
        Log.d(TAG, "Stopping voice recognition service")
        // Release microphone when voice trigger is disabled
        MicrophoneStateManager.releaseMicrophone()
        VoiceRecognitionService.stopService(context)
    }

    /**
     * Update the voice recognition service based on user preferences
     */
    fun updateVoiceRecognition(context: Context, enabled: Boolean) {
        managerScope.launch {
            // Check microphone availability before enabling
            if (enabled) {
                val isMicAvailable = MicrophoneStateManager.checkMicrophoneAvailability(context)
                if (isMicAvailable) {
                    startVoiceRecognition(context)
                } else {
                    Log.d(TAG, "Cannot enable voice recognition: microphone unavailable")
                }
            } else {
                stopVoiceRecognition(context)
            }
        }
    }
}
