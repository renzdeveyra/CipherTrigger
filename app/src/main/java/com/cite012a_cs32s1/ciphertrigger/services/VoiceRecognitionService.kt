package com.cite012a_cs32s1.ciphertrigger.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cite012a_cs32s1.ciphertrigger.MainActivity
import com.cite012a_cs32s1.ciphertrigger.R
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository
import com.cite012a_cs32s1.ciphertrigger.di.AppModule
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Service for continuous voice recognition to detect trigger phrases
 */
class VoiceRecognitionService : Service() {

    companion object {
        private const val TAG = "VoiceRecognitionService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "voice_recognition_channel"

        // Intent actions
        const val ACTION_START_LISTENING = "com.cite012a_cs32s1.ciphertrigger.action.START_LISTENING"
        const val ACTION_STOP_LISTENING = "com.cite012a_cs32s1.ciphertrigger.action.STOP_LISTENING"

        // Start the service
        fun startService(context: Context) {
            val intent = Intent(context, VoiceRecognitionService::class.java).apply {
                action = ACTION_START_LISTENING
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        // Stop the service
        fun stopService(context: Context) {
            val intent = Intent(context, VoiceRecognitionService::class.java).apply {
                action = ACTION_STOP_LISTENING
            }
            context.stopService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var preferencesRepository: PreferencesRepository
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var triggerPhrase = "help me"
    private var voiceTriggerEnabled = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        preferencesRepository = AppModule.providePreferencesRepository(application)

        // Load user preferences
        serviceScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()
            voiceTriggerEnabled = preferences.voiceTriggerEnabled
            triggerPhrase = preferences.voiceTriggerPhrase

            if (voiceTriggerEnabled) {
                startListening()
            }
        }

        // Create notification channel for Android O and above
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with action: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_LISTENING -> {
                startForeground(NOTIFICATION_ID, createNotification())

                serviceScope.launch {
                    val preferences = preferencesRepository.userPreferencesFlow.first()
                    voiceTriggerEnabled = preferences.voiceTriggerEnabled
                    triggerPhrase = preferences.voiceTriggerPhrase

                    if (voiceTriggerEnabled) {
                        startListening()
                    }
                }
            }

            ACTION_STOP_LISTENING -> {
                stopListening()
                stopForeground(true)
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        stopListening()
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Start listening for voice commands
     */
    private fun startListening() {
        if (isListening || !voiceTriggerEnabled) {
            return
        }

        if (!PermissionUtils.hasPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
            Log.e(TAG, "Microphone permission not granted")
            // Automatically disable voice trigger if permission is not granted
            serviceScope.launch {
                preferencesRepository.updateVoiceTriggerSettings(enabled = false, triggerPhrase)
                voiceTriggerEnabled = false
            }
            return
        }

        try {
            // Always get the latest trigger phrase from preferences
            serviceScope.launch {
                val preferences = preferencesRepository.userPreferencesFlow.first()
                triggerPhrase = preferences.voiceTriggerPhrase
                Log.d(TAG, "Updated trigger phrase to: $triggerPhrase")
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(recognitionListener)

            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            }

            speechRecognizer?.startListening(recognizerIntent)
            isListening = true
            Log.d(TAG, "Started listening for voice commands")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition: ${e.message}")
            // Automatically disable voice trigger if there's an error starting recognition
            serviceScope.launch {
                preferencesRepository.updateVoiceTriggerSettings(enabled = false, triggerPhrase)
                voiceTriggerEnabled = false
            }
        }
    }

    /**
     * Stop listening for voice commands
     */
    private fun stopListening() {
        if (!isListening) {
            return
        }

        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
            Log.d(TAG, "Stopped listening for voice commands")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition: ${e.message}")
        }
    }

    /**
     * Recognition listener for speech recognition
     */
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "Beginning of speech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Not used
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Not used
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "End of speech")

            // When speech recognition ends, automatically disable voice trigger
            serviceScope.launch {
                // Update preferences to disable voice trigger
                preferencesRepository.updateVoiceTriggerSettings(enabled = false, triggerPhrase)
                voiceTriggerEnabled = false

                // Release the microphone
                stopListening()

                Log.d(TAG, "Voice trigger automatically disabled after speech recognition ended")
            }
        }

        override fun onError(error: Int) {
            val errorMessage = getErrorText(error)
            Log.e(TAG, "Error in speech recognition: $errorMessage")

            // For certain errors, we should disable voice trigger
            if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ||
                error == SpeechRecognizer.ERROR_AUDIO ||
                error == SpeechRecognizer.ERROR_SERVER) {

                serviceScope.launch {
                    // Update preferences to disable voice trigger
                    preferencesRepository.updateVoiceTriggerSettings(enabled = false, triggerPhrase)
                    voiceTriggerEnabled = false

                    // Release the microphone
                    stopListening()

                    Log.d(TAG, "Voice trigger automatically disabled due to error: $errorMessage")
                }
            } else {
                // For other errors, try to restart listening
                serviceScope.launch {
                    if (voiceTriggerEnabled) {
                        startListening()
                    }
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0].lowercase(Locale.getDefault())
                Log.d(TAG, "Speech recognized: $spokenText")

                // Check if the spoken text contains the trigger phrase
                if (spokenText.contains(triggerPhrase.lowercase(Locale.getDefault()))) {
                    Log.d(TAG, "Trigger phrase detected: $triggerPhrase")
                    triggerAlert()
                }
            }

            // Restart listening
            serviceScope.launch {
                if (voiceTriggerEnabled) {
                    startListening()
                }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0].lowercase(Locale.getDefault())
                Log.d(TAG, "Partial speech recognized: $spokenText")

                // Check if the spoken text contains the trigger phrase
                if (spokenText.contains(triggerPhrase.lowercase(Locale.getDefault()))) {
                    Log.d(TAG, "Trigger phrase detected in partial results: $triggerPhrase")
                    triggerAlert()
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Not used
        }
    }

    /**
     * Trigger the SOS alert
     */
    private fun triggerAlert() {
        // Stop listening temporarily
        stopListening()

        // Launch the alert screen
        val alertIntent = Intent(this, MainActivity::class.java).apply {
            action = "com.cite012a_cs32s1.ciphertrigger.action.TRIGGER_ALERT"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(alertIntent)
    }

    /**
     * Create a notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.voice_recognition_channel_name)
            val description = getString(R.string.voice_recognition_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
                enableVibration(false)
                enableLights(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create a notification for the foreground service
     */
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.voice_recognition_notification_title))
            .setContentText(getString(R.string.voice_recognition_notification_text))
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    /**
     * Get error text for speech recognition errors
     */
    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }
}
