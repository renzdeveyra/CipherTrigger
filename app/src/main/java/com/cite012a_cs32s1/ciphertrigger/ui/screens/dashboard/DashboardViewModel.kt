package com.cite012a_cs32s1.ciphertrigger.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import com.cite012a_cs32s1.ciphertrigger.data.repositories.LocationRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository
import com.cite012a_cs32s1.ciphertrigger.di.AppModule
import com.cite012a_cs32s1.ciphertrigger.services.VoiceRecognitionManager
import com.cite012a_cs32s1.ciphertrigger.utils.MicrophoneStateManager
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Dashboard screen
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = AppModule.providePreferencesRepository(application)
    private val locationRepository = AppModule.provideLocationRepository(application)

    private val _locationPermissionGranted = MutableStateFlow(locationRepository.hasLocationPermission())
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted

    private val _microphonePermissionGranted = MutableStateFlow(PermissionUtils.hasPermission(getApplication(), android.Manifest.permission.RECORD_AUDIO))
    val microphonePermissionGranted: StateFlow<Boolean> = _microphonePermissionGranted

    // Track microphone availability (if it's in use by another app)
    private val _isMicrophoneAvailable = MutableStateFlow(true)
    val isMicrophoneAvailable: StateFlow<Boolean> = _isMicrophoneAvailable

    val dashboardState = combine(
        preferencesRepository.userPreferencesFlow,
        _locationPermissionGranted,
        _microphonePermissionGranted,
        _isMicrophoneAvailable
    ) { preferences, locationPermission, microphonePermission, microphoneAvailable ->
        DashboardState(
            voiceTriggerEnabled = microphonePermission && microphoneAvailable && preferences.voiceTriggerEnabled,
            locationServicesEnabled = locationPermission && preferences.locationSharingEnabled,
            emergencyContacts = preferences.emergencyContacts,
            hasMicrophonePermission = microphonePermission,
            isMicrophoneAvailable = microphoneAvailable
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )

    /**
     * Update voice trigger status
     */
    fun updateVoiceTriggerStatus(enabled: Boolean) {
        viewModelScope.launch {
            // Check microphone permission
            val hasMicPermission = PermissionUtils.hasPermission(getApplication(), android.Manifest.permission.RECORD_AUDIO)
            _microphonePermissionGranted.value = hasMicPermission

            // Check if microphone is available (not in use by another app)
            val isMicAvailable = MicrophoneStateManager.checkMicrophoneAvailability(getApplication())
            _isMicrophoneAvailable.value = isMicAvailable

            // Only enable voice trigger if microphone permission is granted and microphone is available
            if (enabled && (!hasMicPermission || !isMicAvailable)) {
                // Cannot enable without microphone permission or if microphone is in use
                return@launch
            }

            val currentPrefs = preferencesRepository.userPreferencesFlow.first()
            preferencesRepository.updateVoiceTriggerSettings(
                enabled = enabled,
                phrase = currentPrefs.voiceTriggerPhrase
            )

            // Update voice recognition service
            if (enabled) {
                // Keep microphone active when voice trigger is enabled
                MicrophoneStateManager.keepMicrophoneActive()
                VoiceRecognitionManager.startVoiceRecognition(getApplication())
            } else {
                // Release microphone when voice trigger is disabled
                MicrophoneStateManager.releaseMicrophone()
                VoiceRecognitionManager.stopVoiceRecognition(getApplication())
            }
        }
    }

    /**
     * Update location services status
     */
    fun updateLocationServicesStatus(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateLocationSharing(enabled)
        }
    }

    /**
     * Check location permission
     */
    fun checkLocationPermission() {
        _locationPermissionGranted.value = locationRepository.hasLocationPermission()
    }

    /**
     * Check microphone permission and availability
     */
    fun checkMicrophonePermission() {
        _microphonePermissionGranted.value = PermissionUtils.hasPermission(getApplication(), android.Manifest.permission.RECORD_AUDIO)

        // Also check microphone availability
        viewModelScope.launch {
            val isMicAvailable = MicrophoneStateManager.checkMicrophoneAvailability(getApplication())
            _isMicrophoneAvailable.value = isMicAvailable
        }
    }
}

/**
 * State for the Dashboard screen
 */
data class DashboardState(
    val voiceTriggerEnabled: Boolean = false,
    val locationServicesEnabled: Boolean = false,
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val hasMicrophonePermission: Boolean = false,
    val isMicrophoneAvailable: Boolean = true
)
