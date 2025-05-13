package com.cite012a_cs32s1.ciphertrigger.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Utility class to manage and check microphone state
 */
object MicrophoneStateManager {
    private const val TAG = "MicrophoneStateManager"
    
    // Sample rate for audio recording (standard value)
    private const val SAMPLE_RATE = 44100
    
    // Audio channel configuration (mono)
    private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    
    // Audio encoding format
    private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    
    // Buffer size for audio recording
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    )
    
    // StateFlow to track microphone availability
    private val _isMicrophoneAvailable = MutableStateFlow(false)
    val isMicrophoneAvailable: StateFlow<Boolean> = _isMicrophoneAvailable.asStateFlow()
    
    // AudioRecord instance for checking microphone state
    private var audioRecord: AudioRecord? = null
    
    /**
     * Check if the microphone is available for use
     * @return true if the microphone is available, false otherwise
     */
    suspend fun checkMicrophoneAvailability(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            // First check if we have the permission
            if (!PermissionUtils.hasPermission(context, android.Manifest.permission.RECORD_AUDIO)) {
                Log.d(TAG, "Microphone permission not granted")
                _isMicrophoneAvailable.value = false
                return@withContext false
            }
            
            try {
                // Release any existing AudioRecord instance
                releaseAudioRecord()
                
                // Create a new AudioRecord instance
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    BUFFER_SIZE
                )
                
                // Check if the AudioRecord instance is initialized
                val state = audioRecord?.state ?: AudioRecord.STATE_UNINITIALIZED
                val isAvailable = state == AudioRecord.STATE_INITIALIZED
                
                Log.d(TAG, "Microphone availability check: $isAvailable")
                _isMicrophoneAvailable.value = isAvailable
                
                // If not available, release the AudioRecord instance
                if (!isAvailable) {
                    releaseAudioRecord()
                }
                
                return@withContext isAvailable
            } catch (e: Exception) {
                Log.e(TAG, "Error checking microphone availability: ${e.message}")
                _isMicrophoneAvailable.value = false
                releaseAudioRecord()
                return@withContext false
            }
        }
    }
    
    /**
     * Keep the microphone active to prevent other apps from using it
     * Call this when voice trigger is enabled
     */
    fun keepMicrophoneActive() {
        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
            try {
                // Start recording to keep the microphone active
                audioRecord?.startRecording()
                Log.d(TAG, "Microphone kept active for voice trigger")
            } catch (e: Exception) {
                Log.e(TAG, "Error keeping microphone active: ${e.message}")
            }
        }
    }
    
    /**
     * Release the microphone when voice trigger is disabled
     */
    fun releaseMicrophone() {
        releaseAudioRecord()
    }
    
    /**
     * Release the AudioRecord instance
     */
    private fun releaseAudioRecord() {
        try {
            audioRecord?.let {
                if (it.state == AudioRecord.STATE_INITIALIZED) {
                    it.stop()
                }
                it.release()
                Log.d(TAG, "AudioRecord released")
            }
            audioRecord = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioRecord: ${e.message}")
        }
    }
}
