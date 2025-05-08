package com.cite012a_cs32s1.ciphertrigger.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.SpeechRecognizer
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import com.cite012a_cs32s1.ciphertrigger.data.models.UserPreferences
import org.mockito.Mockito.times

@RunWith(MockitoJUnitRunner::class)
class VoiceRecognitionServiceTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPreferencesRepository: PreferencesRepository

    @Mock
    private lateinit var mockSpeechRecognizer: SpeechRecognizer

    @Mock
    private lateinit var mockIntent: Intent

    private val userPreferencesFlow = MutableStateFlow(
        UserPreferences(
            isSetupCompleted = true,
            voiceTriggerEnabled = true,
            voiceTriggerPhrase = "help me"
        )
    )

    @Before
    fun setup() {
        runBlocking {
            `when`(mockPreferencesRepository.userPreferencesFlow).thenReturn(userPreferencesFlow)
        }
    }

    @Test
    fun `test trigger phrase detection`() = runBlocking {
        // Given
        val triggerPhrase = userPreferencesFlow.first().voiceTriggerPhrase
        val recognitionResults = Bundle().apply {
            putStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION,
                arrayListOf("I need $triggerPhrase right now")
            )
        }

        // When a phrase containing the trigger is detected
        val listener = VoiceRecognitionService().javaClass
            .getDeclaredField("recognitionListener")
            .apply { isAccessible = true }
            .get(VoiceRecognitionService()) as RecognitionListener

        listener.onResults(recognitionResults)

        // Then the alert should be triggered
        // Note: This is a simplified test. In a real test environment, you would
        // verify that the appropriate method was called to trigger the alert.
    }

    @Test
    fun `test voice recognition manager initialization`() {
        // Given
        val context = mockContext
        
        // When
        VoiceRecognitionManager.initialize(context, mockPreferencesRepository)
        
        // Then
        // In a real test, you would verify that the service was started
        // This is a simplified test to demonstrate the concept
    }
}
